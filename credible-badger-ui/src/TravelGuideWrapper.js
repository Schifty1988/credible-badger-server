import './App.css';
import React from "react";
import { useNavigate } from 'react-router-dom';
import TravelGuide from './TravelGuide';

const TravelGuideWrapper = () => {
    const navigate = useNavigate();
    return (
        <div className="content">
            <div className="user-info">
                <h2>Credible Badger</h2>
                <button type="button" className="red-button" onClick={() => navigate('/')}>Login</button>
            </div>
            <TravelGuide />
        </div>
    );
};

export default TravelGuideWrapper;
