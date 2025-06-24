import http from 'k6/http';
import { check, sleep } from 'k6';

// Options for the test
export const options = {
    scenarios: {
        basic: {
            executor: 'constant-vus',
            vus: 5,
            duration: '10s',
        },
        load: {
            /*
                Ramping up the number of virtual users (VUs) over time.
                - Start with 0 VUs.
                - Ramp up to 15 VUs over 1 minute.
                - Maintain 15 VUs for another minute.
                - Ramp down to 0 VUs over 30 seconds.
                - Gracefully ramp down over 5 seconds.
            */
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 15 },
                { duration: '1m', target: 15 },
                { duration: '30s', target: 0 },
            ],
            // This is the time to wait before starting the ramp down phase.
            gracefulRampDown: '5s',
        },
    },
    thresholds: {
        'checks': ['rate>0.95'],
        'http_req_duration': ['p(95)<600'],
    },
};

export default function () {
    // Make HTTP GET request to summation endpoint
    const response = http.get('http://localhost:8082/api/summation');

    // Check if the response status is 200 OK
    check(response, {
        'is status 200': (r) => r.status === 200,
        'response body is valid number': (r) => !isNaN(parseInt(r.body))
    });
    
    // Log the summation value
    console.log(`Summation value: ${response.body}`);
    
    // Add a small pause between requests
    sleep(1);
}