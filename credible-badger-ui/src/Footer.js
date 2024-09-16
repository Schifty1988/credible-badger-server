import './App.css';
import React from "react";
import { useNavigate } from 'react-router-dom';

const Footer = () => {
    const navigate = useNavigate();
    
    return (
        <React.Fragment>
            <button onClick={() => window.location.href = 'mailto:support@credibleanimals.com?subject=Travel%20Guide%20Feedback'}>Give Us Feedback</button>
            <button onClick={() => navigate("/legal")}>Terms of Service & Privacy Policy</button>
        </React.Fragment>
    );
};

export default Footer;
