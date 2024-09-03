import './App.css';
import React from "react";
import { useNavigate } from 'react-router-dom';
import TravelGuide from './TravelGuide';

const TravelGuideWrapper = () => {
    const navigate = useNavigate();
    return (
        <div className="Content">
            <div className="UserInfo">
                <h2>Credible Badger</h2>
                <button type="button" onClick={() => navigate('/')}>Login</button>
            </div>
            <TravelGuide />
        </div>
    );
};

export default TravelGuideWrapper;
