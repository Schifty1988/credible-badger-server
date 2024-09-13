import './App.css';
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Login from './Login';
import Legal from './Legal';
import Dashboard from './Dashboard';
import Storage from './Storage';
import Feedback from './Feedback';
import Admin from './Admin';
import PasswordChange from './PasswordChange';
import EmailVerification from './EmailVerification';
import TravelGuideWrapper from './TravelGuideWrapper';
import { UserProvider } from './UserContext';

const App = () => {
    return (
        <UserProvider>
            <Router>
                <div className="App">
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/travelGuide/:guideLink?" element={<TravelGuideWrapper />} />
                        <Route path="/storage" element={<Storage />} />
                        <Route path="/feedback" element={<Feedback />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/changePassword/:token?" element={<PasswordChange />} />
                        <Route path="/verifyEmail/:token?" element={<EmailVerification />} />
                        <Route path="/legal" element={<Legal />} />
                        <Route exact path="/" element={<Dashboard />} />    
                    </Routes>
                </div>
            </Router>
        </UserProvider>
    );
};

export default App;
