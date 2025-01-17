import './App.css';
import React from "react";
import { useNavigate } from 'react-router-dom';
import MovieGuide from './MovieGuide';

const MovieGuideWrapper = () => {
    const navigate = useNavigate();
    return (
        <div className="content">
            <div className="user-info">
                <img alt="Credible Badger" className="logo" src="/logo_title.png"/>
                <button type="button" className="red-button" onClick={() => navigate('/')}>Login</button>
            </div>
            <MovieGuide />
        </div>
    );
};

export default MovieGuideWrapper;
