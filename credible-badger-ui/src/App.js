import './App.css';
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Login from './Login';
import Legal from './Legal';
import Storage from './Storage';
import Feedback from './Feedback';
import Admin from './Admin';
import Story from './Story';
import PasswordChange from './PasswordChange';
import EmailVerification from './EmailVerification';
import TravelGuide from './TravelGuide';
import MovieGuide from './MovieGuide';
import BookGuide from './BookGuide';
import { UserProvider } from './UserContext';

const App = () => {
    return (
        <UserProvider>
            <Router>
                <div className="app">
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/travelGuide/:guideLink?" element={<TravelGuide />} />
                        <Route path="/movieGuide/:guideLink?" element={<MovieGuide />} />
                        <Route path="/bookGuide/:guideLink?" element={<BookGuide />} />
                        <Route path="/storage" element={<Storage />} />
                        <Route path="/feedback" element={<Feedback />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/story" element={<Story />} />
                        <Route path="/changePassword/:token?" element={<PasswordChange />} />
                        <Route path="/verifyEmail/:token?" element={<EmailVerification />} />
                        <Route path="/legal" element={<Legal />} />
                        <Route exact path="/" element={<TravelGuide />} />    
                    </Routes>
                </div>
            </Router>
        </UserProvider>
    );
};

export default App;
