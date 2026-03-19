const http = require('http');

const data = JSON.stringify({
    username: 'test_node_creator2',
    email: 'test_node2@test.com',
    password: 'password123',
    confirmPassword: 'password123',
    role: 'CREATER'
});

const options = {
    hostname: 'localhost',
    port: 9999,
    path: '/auth/register',
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length
    }
};

const req = http.request(options, (res) => {
    console.log(`STATUS: ${res.statusCode}`);
    res.on('data', (d) => {
        process.stdout.write(d);
    });
});

req.on('error', (error) => {
    console.error(error);
});

req.write(data);
req.end();
