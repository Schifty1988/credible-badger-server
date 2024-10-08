import './App.css';
import React, { useState } from "react";
import { useParams, useNavigate  } from 'react-router-dom';
import Footer from './Footer';

const PasswordChange = () => {
    const { token } = useParams();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [actionResponse, setActionResponse] = useState([]); 
    const [responseType, setResponseType] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const [showNotification, setShowNotification] = useState(false);    
    const navigate = useNavigate();
  
    const handleEmailChange = (event) => {
        setEmail(event.target.value);
    };
    
    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };
    
    const hasValidToken = () => {
        return token && token.length > 0;
    };

    const requestNewPassword = () => {
        fetch(`${apiUrl}/api/user/requestPasswordChange`, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({email: email}) 
        })
        .then(response => { 
            if (response.ok) {          
                displayActionResponse("Check your email for instructions to reset your password.", true);
            }
            else {
                displayActionResponse("An error occured: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("An error occured: " + error.message, false);
        });
    };
    
    const changePassword = () => {
        fetch(`${apiUrl}/api/user/changePassword`, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({password : password, securityToken: token}) 
        })
        .then(response => { 
            if (response.ok) {
                navigate("/");
            }
            else {
                displayActionResponse("Setting the new password failed: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Setting the new password failed: " + error.message, false);
        });
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
    
    return (
        <div className="content">

            <h2>Password Change</h2>
            {!hasValidToken() ? (              
                <div className="content-group">
                    <input type='text' id="email" placeholder="Email" value={email} onChange={handleEmailChange}/>
                    <button type="button" onClick={requestNewPassword}>Request Password Change</button>
                </div>
            ) : (
                <div className="content-group">
                    <input type='password' id="password" placeholder="Password" value={password} onChange={handlePasswordChange}/>
                    <button type="button" onClick={changePassword}>Change Password</button>
                </div>
            )}
            {showNotification && (
            <div className={responseType ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
            <div className="footer">
                <Footer/>
            </div>
        </div>
    );
};

export default PasswordChange;
