const axios = require('axios');

async function testProfileApi() {
    try {
        console.log("Logging in...");
        const loginRes = await axios.post('http://localhost:9999/auth/login', {
            username: 'test@example.com',
            password: 'Password123!'
        });

        const token = loginRes.data.token;
        console.log("Login successful! Token acquired.");

        console.log("\nFetching /userProfile/me...");
        const profileRes = await axios.get('http://localhost:9999/userProfile/me', {
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Success! Status:", profileRes.status);
        console.log("Data size:", JSON.stringify(profileRes.data).length, "bytes");

        console.log("\nFetching /revconnect/users/getAllposts...");
        const postsRes = await axios.get('http://localhost:9999/revconnect/users/getAllposts', {
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Success! Status:", postsRes.status);
        console.log("Data size:", JSON.stringify(postsRes.data).length, "bytes");

    } catch (err) {
        console.error("\n[ERROR CAUGHT]");
        console.error("Status:", err.response?.status);
        console.error("Error Body:", err.response?.data);
        console.error("Message:", err.message);
    }
}

testProfileApi();
