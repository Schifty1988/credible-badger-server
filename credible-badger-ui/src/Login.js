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
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown',
        ERROR_USERNAME: 'error_user',
        ERROR_PASSWORD: 'error_password'
    };
    
    const validateInput = () => {
        if (!clientValidationUser()) {
            displayActionResponse("The provided email seems invalid.", ResponseTypes.ERROR_USERNAME);
            return false;
        }
        
        if (!clientValidationPassword()) {
            displayActionResponse("The password must be at least 6 characters long.", ResponseTypes.ERROR_PASSWORD);
            return false;
        }
        
        return true;
    };    

    const clientValidationUser = () => {
        return username.length > 5 && username.indexOf('@') !== -1;
    };
    
    const clientValidationPassword = () => {
        return password.length >= 6;
    };
    
    const register = () => {      
        if (!validateInput()) {
            return;
        }
        
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
                displayActionResponse("Registration failed: " + response.status, ResponseTypes.ERROR_UNKNOWN);
            }
        })
        .catch(error => {
            displayActionResponse("Registration failed: " + error.message, ResponseTypes.ERROR_UNKNOWN);
        });
    };    
    
    const login = () => {
        if (!validateInput()) {
            return;
        }
        
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
                displayActionResponse("Login failed: " + response.status, ResponseTypes.ERROR_UNKNOWN);
            }
        })
        .catch(error => {
            displayActionResponse("Login failed: " + error.message, ResponseTypes.ERROR_UNKNOWN);
        });
    };
    
    const handleUsernameChange = (event) => {
        setUsername(event.target.value);
    };
    
    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };
    
    const displayActionResponse = (message, responseType) => {
        setResponseType(responseType);
        setActionResponse(message);
    };

    return (
        <div className="Content">
            <h2>Credible Badger</h2>

            <div className="content-group">
                <input className={responseType === ResponseTypes.ERROR_USERNAME ? "error-highlight" : ""} type="text" placeholder="Email" id="email" value={username} onChange={handleUsernameChange}/>
                <input className={responseType === ResponseTypes.ERROR_PASSWORD ? "error-highlight" : ""} type="password" placeholder="Password" id="password" value={password} onChange={handlePasswordChange}/>
                <button type="button" onClick={login}>Login</button>
                <button type="button" onClick={register}>Register</button>
            </div>
            <p>Forgot you password? Change it <Link to="/changePassword">here</Link>!</p>
            <p className={responseType === ResponseTypes.SUCCESS ? "success" : "error"}>{actionResponse}</p>
        </div>
    );
};

export default Login;
