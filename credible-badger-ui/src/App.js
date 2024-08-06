import './App.css';
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Login from './Login';
import Dashboard from './Dashboard';
import Admin from './Admin';
import PasswordChange from './PasswordChange';
import EmailVerification from './EmailVerification';
import { UserProvider } from './UserContext';

const App = () => {
    return (
        <UserProvider>
            <Router>
                <div className="App">
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/admin" element={<Admin />} />
                        <Route path="/changePassword/:token?" element={<PasswordChange />} />
                        <Route path="/verifyEmail/:token?" element={<EmailVerification />} />
                        <Route exact path="/" element={<Dashboard />} />    
                    </Routes>
                </div>
            </Router>
        </UserProvider>
    );
};

export default App;
