import './App.css';
import React, { useContext, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import { UserContext } from './UserContext';

const UserInfo = () => {
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const apiUrl = process.env.REACT_APP_API_URL;
    
    useEffect(() => {
        fetch(`${apiUrl}/api/user/me`, {
            method: 'GET',
            credentials: 'include'})
            .then(response => {
                if (!response.ok) {
                    navigate('/login');
                    return {email: "Unknown"};
                }
                return response.json();
            })
            .then(data => setUser(data))
            .catch(error => {
                navigate('/login');
            });
    }, []);

    const callUserLogout = (event) => {
        event.preventDefault();
        fetch(`${apiUrl}/api/user/logout`, {credentials: 'include'})
            .then(response => {
                if (response.ok) {
                    setUser(null);
                    navigate("/login");
                }
                response.text();
            })
            .catch(error => {
                console.log('Error occured during logout!');
            });
    };

    return (
        <div className="UserInfo">
            <h2>{user ? user.email : 'Loading...'}</h2>
            <button type="button" onClick={callUserLogout}>Logout</button>
        </div>
    );
};

export default UserInfo;
