import './App.css';
import React, { useState } from "react";
import { useNavigate, Link } from 'react-router-dom';

const Login = () => {
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);    
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");    
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    
    const register = () => {
        fetch(`${apiUrl}/api/user/register`, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({email: username, password : password}) 
        })
        .then(response => { 
            if (response.ok) {
                login();
            } else {
                displayActionResponse("Registration failed: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Registration failed: " + error.message, false);
        });
    };    
    
    const login = () => {
        const formDataLogin = new FormData();
        formDataLogin.append('username', username);
        formDataLogin.append('password', password);
        
        fetch(`${apiUrl}/api/user/login`, {
            method: 'POST',
            credentials: 'include',
            body: formDataLogin
        })
        .then(response => {
            if (response.ok) {
                navigate('/');
            } else {
                displayActionResponse("Login failed: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Login failed: " + error.message, false);
        });
    };
    
    const handleUsernameChange = (event) => {
        setUsername(event.target.value);
    };
    
    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };
    
    const displayActionResponse = (message, wasSuccessful) => {
        setResponseType(wasSuccessful ? "success" : "error");
        setActionResponse(message);
    };

    return (
        <div className="Content">
            <h2>Credible Badger</h2>

            <div className="content-group">
                <input type="text" placeholder="Email" id="email" value={username} onChange={handleUsernameChange}/>
                <input type="password" placeholder="Password" id="password" value={password} onChange={handlePasswordChange}/>
                <button type="button" onClick={login}>Login</button>
                <button type="button" onClick={register}>Register</button>
            </div>
            <p>Forgot you password? Change it <Link to="/changePassword">here</Link>!</p>
            <p className={responseType}>{actionResponse}</p>
        </div>
    );
};

export default Login;
