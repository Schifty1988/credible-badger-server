import './App.css';
import React, { useState, useEffect } from "react";
import { useParams, useNavigate  } from 'react-router-dom';
import Footer from './Footer';

const MarketingOptOut = () => {
    const { token } = useParams();
    const [email, setEmail] = useState("");
    const [unsubscribed, setUnsubscribed] = useState(false);
    const navigate = useNavigate();
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const [showNotification, setShowNotification] = useState(false);   
    
    const hasValidToken = () => {
        return token && token.length > 0;
    };

    const disableMarketingSubscription = () => {
        fetch(`${apiUrl}/api/user/disableMarketingSubscription/${token}`, {
            method: 'POST'
        })
        .then(response => {
            if (response.ok) {
                displayActionResponse("You have unsubscribed successfully!", true);
                setUnsubscribed(true);
            }
            else {
                displayActionResponse("Something went wrong: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Something went wrong: " + error.message, false);
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
                <h2>Unsubscribe from Marketing</h2>
            
            {!hasValidToken() ? (
                <p>Something went wrong!</p>
            ) : 
            (
                unsubscribed ? (
                    <p>You will no longer receive marketing emails!</p>
                ) : (
                    <div className="content-group">
                        <p>Stop receiving marketing emails by clicking on this button</p>
                        <button type="button" onClick={disableMarketingSubscription}>Unsubscribe</button>
                    </div>
                )
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

export default MarketingOptOut;
