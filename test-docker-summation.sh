#!/bin/bash

# Docker-specific validation script for summation database update fixes
echo "🐳 DOCKER SUMMATION VALIDATION SCRIPT"
echo "====================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check if Docker containers are running
echo "1. CHECKING DOCKER CONTAINERS"
echo "-----------------------------"

print_info "Checking if Service-B container is running..."
if docker ps | grep -q "service-b"; then
    print_status 0 "Service-B container is running"
else
    print_status 1 "Service-B container is NOT running"
    echo "Start with: cd ../Docker-Compose && docker-compose up -d service-b"
fi

print_info "Checking if PostgreSQL container is running..."
if docker ps | grep -q "postgres-db"; then
    print_status 0 "PostgreSQL container is running"
else
    print_status 1 "PostgreSQL container is NOT running"
    echo "Start with: cd ../Docker-Compose && docker-compose up -d postgres"
fi

print_info "Checking if Kafka container is running..."
if docker ps | grep -q "kafka"; then
    print_status 0 "Kafka container is running"
else
    print_status 1 "Kafka container is NOT running"
    echo "Start with: cd ../Docker-Compose && docker-compose up -d kafka"
fi

echo ""

# Test database connectivity from host
echo "2. TESTING DATABASE CONNECTIVITY"
echo "--------------------------------"

print_info "Testing Leader Database (outbox_write) via Docker port 5434..."
if psql -h localhost -p 5434 -U postgres -d outbox_write -c "SELECT 1;" > /dev/null 2>&1; then
    print_status 0 "Leader Database accessible from host"
    
    # Check schema
    LEADER_SCHEMA=$(psql -h localhost -p 5434 -U postgres -d outbox_write -t -c "SELECT data_type FROM information_schema.columns WHERE table_name = 'outbox' AND column_name = 'id';" 2>/dev/null | tr -d ' ')
    if [[ $LEADER_SCHEMA == "bigint" ]]; then
        print_status 0 "Leader Database schema correct (id: bigint)"
    else
        print_status 1 "Leader Database schema incorrect (id: $LEADER_SCHEMA, expected: bigint)"
    fi
    
    # Check initial record
    LEADER_RECORD=$(psql -h localhost -p 5434 -U postgres -d outbox_write -t -c "SELECT id, sum FROM outbox WHERE id = 1;" 2>/dev/null | tr -d ' ')
    if [[ -n $LEADER_RECORD ]]; then
        print_status 0 "Leader Database has record with id=1: $LEADER_RECORD"
    else
        print_status 1 "Leader Database missing record with id=1"
    fi
else
    print_status 1 "Leader Database NOT accessible"
fi

print_info "Testing Replica Database (outbox_read) via Docker port 5434..."
if psql -h localhost -p 5434 -U postgres -d outbox_read -c "SELECT 1;" > /dev/null 2>&1; then
    print_status 0 "Replica Database accessible from host"
    
    # Check schema
    REPLICA_SCHEMA=$(psql -h localhost -p 5434 -U postgres -d outbox_read -t -c "SELECT data_type FROM information_schema.columns WHERE table_name = 'outbox' AND column_name = 'id';" 2>/dev/null | tr -d ' ')
    if [[ $REPLICA_SCHEMA == "bigint" ]]; then
        print_status 0 "Replica Database schema correct (id: bigint)"
    else
        print_status 1 "Replica Database schema incorrect (id: $REPLICA_SCHEMA, expected: bigint)"
    fi
    
    # Check initial record
    REPLICA_RECORD=$(psql -h localhost -p 5434 -U postgres -d outbox_read -t -c "SELECT id, sum FROM outbox WHERE id = 1;" 2>/dev/null | tr -d ' ')
    if [[ -n $REPLICA_RECORD ]]; then
        print_status 0 "Replica Database has record with id=1: $REPLICA_RECORD"
    else
        print_status 1 "Replica Database missing record with id=1"
    fi
else
    print_status 1 "Replica Database NOT accessible"
fi

echo ""

# Test Service-B endpoints
echo "3. TESTING SERVICE-B ENDPOINTS"
echo "------------------------------"

print_info "Testing Service-B health endpoint..."
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    print_status 0 "Service-B health endpoint accessible"
else
    print_status 1 "Service-B health endpoint NOT accessible"
fi

print_info "Testing Service-B summation endpoint..."
SUMMATION_RESPONSE=$(curl -s http://localhost:8082/api/summation 2>/dev/null)
if [[ $SUMMATION_RESPONSE =~ ^[0-9]+$ ]]; then
    print_status 0 "GET /api/summation returns: $SUMMATION_RESPONSE"
else
    print_status 1 "GET /api/summation failed or invalid response: $SUMMATION_RESPONSE"
fi

echo ""

# Check Service-B logs for errors
echo "4. CHECKING SERVICE-B LOGS"
echo "--------------------------"

print_info "Checking recent Service-B container logs for errors..."
ERROR_COUNT=$(docker logs service-b --tail 50 2>/dev/null | grep -i "error\|exception\|failed" | wc -l)
if [ $ERROR_COUNT -eq 0 ]; then
    print_status 0 "No recent errors found in Service-B logs"
else
    print_status 1 "Found $ERROR_COUNT error(s) in recent Service-B logs"
    echo "Check logs with: docker logs service-b"
fi

print_info "Checking for successful database initialization..."
if docker logs service-b 2>/dev/null | grep -q "Initialized summation record"; then
    print_status 0 "Database initialization successful"
else
    print_status 1 "Database initialization not found in logs"
fi

echo ""

# Test Kafka integration
echo "5. TESTING KAFKA INTEGRATION"
echo "----------------------------"

print_info "Checking if Service-B is consuming from Kafka..."
if docker logs service-b 2>/dev/null | grep -q "KafkaConsumerService"; then
    print_status 0 "Kafka consumer service is active"
else
    print_status 1 "Kafka consumer service not found in logs"
fi

print_info "To test end-to-end Kafka flow:"
echo "  1. Send a request to Service-A: curl -X POST http://localhost:8090/api/sum -H 'Content-Type: application/json' -d '{\"value\": 5}'"
echo "  2. Check Service-B logs: docker logs -f service-b"
echo "  3. Verify database update: psql -h localhost -p 5434 -U postgres -d outbox_write -c \"SELECT * FROM outbox WHERE id = 1;\""

echo ""

# Summary
echo "6. VALIDATION SUMMARY"
echo "--------------------"

print_info "Configuration fixes applied:"
echo "  ✓ Updated application.properties to use environment variables"
echo "  ✓ Fixed database configurations to work with Docker"
echo "  ✓ Changed Outbox entity to use fixed ID=1"
echo "  ✓ Updated Docker database initialization script"
echo "  ✓ Enhanced transaction management with flush() calls"

echo ""
print_info "Docker Environment Details:"
echo "  • Service-B: http://localhost:8082"
echo "  • PostgreSQL: localhost:5434 (mapped from container port 5432)"
echo "  • Kafka: localhost:9092"
echo "  • Service-A Load Balancer: http://localhost:8090"

echo ""
echo "🎉 Docker validation complete!"