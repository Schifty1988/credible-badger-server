import './App.css';
import React, { useState } from "react";
import { useNavigate, Link } from 'react-router-dom';
import Footer from './Footer';

const Login = () => {
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [registrationMode, setRegistrationMode] = useState(false);
    const [acceptTerms, setAcceptTerms] = useState("");
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    const [showNotification, setShowNotification] = useState(false);
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown',
        ERROR_USERNAME: 'error_user',
        ERROR_PASSWORD: 'error_password',
        ERROR_ACCEPT_TERMS: 'error_terms'
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
        
        if (!acceptTerms) {
            displayActionResponse("Terms of Service must be read and accepted.", ResponseTypes.ERROR_ACCEPT_TERMS);
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

        if (message.length > 0) {
            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
            }, 3000); 
        }
    };
    
    const handleAcceptTermsChange = (event) => {
        setAcceptTerms(event.target.checked);
    };
    
    const activateRegistrationMode = (event) =>  {
        setRegistrationMode(true);
    };
    
    const activateLoginMode = (event) =>  {
        setRegistrationMode(false);
    };

    return (
        <div className="content">
            <img className="logo" src="logo_title.png"/>
            <div className="content-group">
                <input className={responseType === ResponseTypes.ERROR_USERNAME ? "error-highlight" : ""} type="text" placeholder="Email" id="email" value={username} onChange={handleUsernameChange}/>
                <input className={responseType === ResponseTypes.ERROR_PASSWORD ? "error-highlight" : ""} type="password" placeholder="Password" id="password" value={password} onChange={handlePasswordChange}/>
                { registrationMode 
                ? <div className="content-group"> 
                    <span className={responseType === ResponseTypes.ERROR_ACCEPT_TERMS ? "error-highlight" : ""}>
                        <input id="terms" type="checkbox" checked={acceptTerms} onChange={handleAcceptTermsChange} />
                        I accept the Terms of Service
                    </span>
                    <button type="button" onClick={register}>Register</button>
                    <span>
                        <Link onClick={activateLoginMode}>You already have an account?</Link>
                    </span>
                </div>
                : <div className="content-group">
                    <button type="button" onClick={login}>Login</button>
                    <span>
                        <Link to="/changePassword">Forgot your password?</Link> <Link onClick={activateRegistrationMode}>Need an account?</Link>
                    </span>
                 </div>
                }
            </div>
            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
            <div className="footer">
                <Footer/>
            </div>
        </div>
    );
};

export default Login;
