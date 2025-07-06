import './App.css';
import React, { useContext, useEffect, useState } from "react";
import { useNavigate, useLocation } from 'react-router-dom';
import { UserContext } from './UserContext';

const UserInfo = () => {
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const location = useLocation();
    const [currentPage, setCurrentPage] = useState(getCurrentPage());
    const apiUrl = process.env.REACT_APP_API_URL;
    
    useEffect(() => {
        fetch(`${apiUrl}/api/user/me`, {
            method: 'GET',
            credentials: 'include'})
            .then(response => {
                if (!response.ok) {
                    return null;
                }
                return response.json();
            })
            .then(data => {
                if (!data) {
                    data = { anonymous : true };
                }
                setUser(data);
            })
            .catch(error => {
                setUser({ anonymous : true });
            });
        setCurrentPage(getCurrentPage());
    }, [apiUrl, setUser]);
    
    function getCurrentPage() {
        return "/" + location.pathname.split('/')[1];
    };
    
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
            case '/activity':
                navigate('/activity');
                break;
            case '/admin':
                navigate('/admin');
                break;
            case '/travelGuide':
                navigate('/travelGuide');
                break;
            case '/movieGuide':
                navigate('/movieGuide');
                break;
            case '/bookGuide':
                navigate('/bookGuide');
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
            default:
                navigate('/activity');
        }
    };

    return (
        <div className="user-info">
            {user && !user.anonymous ? 
            (<React.Fragment>
                <h2>{user.email}</h2> 
                <select id="navigation" className="select-dropdown" value={currentPage} onChange={handleSelectChange}>
                    <option value="/activity">Activity</option>
                    <option value="/travelGuide">Travel</option>
                    <option value="/movieGuide">Movies</option>
                    <option value="/bookGuide">Books</option>
                    <option value="/storage">Storage</option>
                    <option value="/feedback">Feedback</option>
                    {isAdmin() && <option value="/admin">Admin</option>}
                    <option value="logout">Logout</option>
                </select>
            </React.Fragment>
            ) : 
            (<React.Fragment>
                <img alt="Credible Badger" className="logo" src="/logo_title.png"/>
                <button type="button" className="red-button" onClick={() => navigate('/login')}>Login</button>
            </React.Fragment>)}
        </div>
    );
};

export default UserInfo;
