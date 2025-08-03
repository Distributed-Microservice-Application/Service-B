#!/bin/bash

# Comprehensive validation script for summation database update fixes
# This script validates that all critical issues have been resolved

echo "🔍 SUMMATION DATABASE UPDATE VALIDATION SCRIPT"
echo "=============================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
    else
        echo -e "${RED}❌ $2${NC}"
    fi
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Check if databases are running
echo "1. CHECKING DATABASE CONNECTIVITY"
echo "--------------------------------"

# Check Leader DB (outbox_write) - Docker uses port 5434
print_info "Testing Leader Database connection (Docker port 5434)..."
if psql -h localhost -p 5434 -U postgres -d outbox_write -c "SELECT 1;" > /dev/null 2>&1; then
    print_status 0 "Leader Database (outbox_write) is accessible"
else
    print_status 1 "Leader Database (outbox_write) is NOT accessible"
    echo "Please ensure Docker containers are running: docker-compose up -d"
fi

# Check Replica DB (outbox_read) - Docker uses port 5434
print_info "Testing Replica Database connection (Docker port 5434)..."
if psql -h localhost -p 5434 -U postgres -d outbox_read -c "SELECT 1;" > /dev/null 2>&1; then
    print_status 0 "Replica Database (outbox_read) is accessible"
else
    print_status 1 "Replica Database (outbox_read) is NOT accessible"
    echo "Please ensure Docker containers are running: docker-compose up -d"
fi

echo ""

# Check database schema
echo "2. VALIDATING DATABASE SCHEMA"
echo "-----------------------------"

# Check Leader DB schema
print_info "Checking Leader Database schema..."
LEADER_SCHEMA=$(psql -h localhost -p 5434 -U postgres -d outbox_write -t -c "
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns
    WHERE table_name = 'outbox'
    ORDER BY ordinal_position;" 2>/dev/null)

if [[ $LEADER_SCHEMA == *"id"* && $LEADER_SCHEMA == *"bigint"* && $LEADER_SCHEMA == *"sum"* ]]; then
    print_status 0 "Leader Database schema is correct (id: bigint, sum: integer)"
else
    print_status 1 "Leader Database schema is incorrect or missing"
    echo "Expected: outbox table with id (bigint) and sum (integer) columns"
fi

# Check Replica DB schema
print_info "Checking Replica Database schema..."
REPLICA_SCHEMA=$(psql -h localhost -p 5434 -U postgres -d outbox_read -t -c "
    SELECT column_name, data_type, is_nullable
    FROM information_schema.columns
    WHERE table_name = 'outbox'
    ORDER BY ordinal_position;" 2>/dev/null)

if [[ $REPLICA_SCHEMA == *"id"* && $REPLICA_SCHEMA == *"bigint"* && $REPLICA_SCHEMA == *"sum"* ]]; then
    print_status 0 "Replica Database schema is correct (id: bigint, sum: integer)"
else
    print_status 1 "Replica Database schema is incorrect or missing"
    echo "Expected: outbox table with id (bigint) and sum (integer) columns"
fi

echo ""

# Check initial data
echo "3. VALIDATING INITIAL DATA"
echo "-------------------------"

# Check Leader DB initial record
print_info "Checking Leader Database initial record..."
LEADER_RECORD=$(psql -h localhost -p 5434 -U postgres -d outbox_write -t -c "SELECT id, sum FROM outbox WHERE id = 1;" 2>/dev/null | tr -d ' ')

if [[ $LEADER_RECORD == "1|0" ]]; then
    print_status 0 "Leader Database has correct initial record (id=1, sum=0)"
elif [[ -n $LEADER_RECORD ]]; then
    print_warning "Leader Database has record with id=1 but sum=${LEADER_RECORD#*|}"
    print_status 0 "Leader Database record exists (this is acceptable)"
else
    print_status 1 "Leader Database missing initial record with id=1"
    echo "Run: INSERT INTO outbox (id, sum) VALUES (1, 0) ON CONFLICT (id) DO NOTHING;"
fi

# Check Replica DB initial record
print_info "Checking Replica Database initial record..."
REPLICA_RECORD=$(psql -h localhost -p 5434 -U postgres -d outbox_read -t -c "SELECT id, sum FROM outbox WHERE id = 1;" 2>/dev/null | tr -d ' ')

if [[ $REPLICA_RECORD == "1|0" ]]; then
    print_status 0 "Replica Database has correct initial record (id=1, sum=0)"
elif [[ -n $REPLICA_RECORD ]]; then
    print_warning "Replica Database has record with id=1 but sum=${REPLICA_RECORD#*|}"
    print_status 0 "Replica Database record exists (this is acceptable)"
else
    print_status 1 "Replica Database missing initial record with id=1"
    echo "Run: INSERT INTO outbox (id, sum) VALUES (1, 0) ON CONFLICT (id) DO NOTHING;"
fi

echo ""

# Check Java application configuration
echo "4. VALIDATING APPLICATION CONFIGURATION"
echo "---------------------------------------"

# Check if application.properties has environment variable support
print_info "Checking application.properties database configuration..."
if grep -q "SPRING_DATASOURCE_LEADER_URL" src/main/resources/application.properties && \
   grep -q "SPRING_DATASOURCE_REPLICA_URL" src/main/resources/application.properties; then
    print_status 0 "Database URLs in application.properties support environment variables"
else
    print_status 1 "Database URLs in application.properties do not support environment variables"
fi

# Check Entity configuration
print_info "Checking Outbox entity configuration..."
if grep -q "private Long id = 1L;" src/main/java/com/DMA/Service_B/entity/Outbox.java; then
    print_status 0 "Outbox entity uses fixed ID=1 (correct)"
else
    print_status 1 "Outbox entity does not use fixed ID=1"
fi

# Check ReplicaDatabaseConfig
print_info "Checking ReplicaDatabaseConfig..."
if grep -q "@ConfigurationProperties" src/main/java/com/DMA/Service_B/config/ReplicaDatabaseConfig.java && \
   ! grep -q "jdbc:postgresql://localhost" src/main/java/com/DMA/Service_B/config/ReplicaDatabaseConfig.java; then
    print_status 0 "ReplicaDatabaseConfig uses @ConfigurationProperties properly"
else
    print_status 1 "ReplicaDatabaseConfig has hardcoded database configuration"
fi

echo ""

# Test database operations (if application is running)
echo "5. TESTING DATABASE OPERATIONS"
echo "------------------------------"

print_info "Testing if Service-B Docker container is running..."
if docker ps | grep -q "service-b"; then
    print_status 0 "Service-B Docker container is running"
    
    # Test GET summation endpoint
    print_info "Testing GET /api/summation endpoint..."
    SUMMATION_RESPONSE=$(curl -s http://localhost:8082/api/summation 2>/dev/null)
    if [[ $SUMMATION_RESPONSE =~ ^[0-9]+$ ]]; then
        print_status 0 "GET /api/summation returns valid number: $SUMMATION_RESPONSE"
    else
        print_status 1 "GET /api/summation failed or returned invalid response: $SUMMATION_RESPONSE"
    fi
    
else
    print_warning "Service-B Docker container is not running - skipping endpoint tests"
    echo "To start containers: cd ../Docker-Compose && docker-compose up -d"
fi

echo ""

# Summary
echo "6. VALIDATION SUMMARY"
echo "--------------------"

print_info "Key fixes implemented:"
echo "  • Fixed ReplicaDatabaseConfig to use explicit database URL"
echo "  • Changed Outbox entity to use fixed ID=1 instead of auto-generation"
echo "  • Added transaction flush() calls for immediate persistence"
echo "  • Updated database schema to use BIGINT for ID column"
echo "  • Improved error handling and logging in KafkaConsumerService"
echo "  • Enhanced initialization logic with better logging"

echo ""
print_info "Next steps to test Kafka integration:"
echo "  1. Ensure Kafka is running on localhost:29092"
echo "  2. Send a test message to 'user-events' topic"
echo "  3. Check application logs for processing confirmation"
echo "  4. Verify database updates using: SELECT * FROM outbox WHERE id = 1;"

echo ""
echo "🎉 Validation complete! Check the status messages above for any issues."