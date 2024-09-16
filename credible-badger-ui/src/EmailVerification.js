import './App.css';
import React, { useState, useEffect } from "react";
import { useParams, useNavigate  } from 'react-router-dom';
import Footer from './Footer';

const EmailVerification = () => {
    const { token } = useParams();
    const [email, setEmail] = useState("");
    const navigate = useNavigate();
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const [showNotification, setShowNotification] = useState(false);   
    
    const hasValidToken = () => {
        return token && token.length > 0;
    };

    const requestEmailVerification = () => {
        fetch(`${apiUrl}/api/user/requestEmailVerification`, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({email: email}) 
        })
        .then(response => {
            if (response.ok) {
                displayActionResponse("Verification email was sent!", true);
            }
            else {
                displayActionResponse("Something went wrong when requesting email verification: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Something went wrong when requesting email verification: " + error.message, false);
        });
    };
    
    const verifyEmail = () => {
        fetch(`${apiUrl}/api/user/verifyEmail`, {
            method: 'POST',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({securityToken: token}) 
        })
        .then(response => { 
            if (response.ok) {
                navigate("/dashboard");
            }
            else {
                displayActionResponse("Email verification failed: " +  response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Something went wrong when requesting email verification: " + error.message, false);
        });
    };

    useEffect(() => {
        if (hasValidToken()) {
            verifyEmail();
        }
     }, []);
    
    const handleEmailChange = (event) => {
        setEmail(event.target.value);
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
                <h2>Email Verification</h2>
            
            {!hasValidToken() ? (
                <div className="content-group">  
                    <input type='email' id="email" placeholder="Email" value={email} onChange={handleEmailChange}/>
                    <button type="button" onClick={requestEmailVerification}>Request Email Verification</button>
                </div>
            ) : 
            (
                <p>Processing Email verification...</p>
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

export default EmailVerification;
