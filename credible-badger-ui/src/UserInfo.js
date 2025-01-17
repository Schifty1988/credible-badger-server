import './App.css';
import React, { useContext, useEffect, useState } from "react";
import { useNavigate, useLocation } from 'react-router-dom';
import { UserContext } from './UserContext';

const UserInfo = () => {
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const location = useLocation();
    const [currentPage, setCurrentPage] = useState(location.pathname);
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

    const callUserLogout = () => {
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
    
    const isAdmin = () => {
        if (!user || !user.roles) {
            return false;
        }
        
        const adminRole = user.roles.find(role => role.role === 'ROLE_ADMIN');
        return adminRole !== undefined;
    };
    
    const handleSelectChange = (event) => {
        setCurrentPage(event.target.value);
        switch (event.target.value) {
            case '/admin':
                navigate('/admin');
                break;
            case '/travel':
                navigate('/travel');
                break;
            case '/movies':
                navigate('/movies');
                break;
            case '/books':
                navigate('/books');
                break;
            case '/storage':
                navigate('/storage');
                break;
            case '/feedback':
                navigate('/feedback');
                break;    
            case 'logout':
                callUserLogout();
            break;
        }
    };

    return (
        <div className="user-info">
            <h2>{user ? user.email : 'Loading...'}</h2>
            <select className="select-dropdown" value={currentPage} onChange={handleSelectChange}>
                <option value="/travel">Travel</option>
                <option value="/movies">Movies</option>
                <option value="/books">Books</option>
                <option value="/storage">Storage</option>
                <option value="/feedback">Feedback</option>
                {isAdmin() && <option value="/admin">Admin</option>}
                <option value="logout">Logout</option>
            </select>
        </div>
    );
};

export default UserInfo;
