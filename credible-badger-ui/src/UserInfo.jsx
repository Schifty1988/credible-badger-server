import './App.css';
import React, { useContext, useEffect } from "react";
import { useNavigate, useLocation } from 'react-router-dom';
import { UserContext } from './UserContext';
import { API_URL, fetchWithAuth } from './Api';
import { logError } from './Logging';

const UserInfo = () => {
    const navigate = useNavigate();
    const { user, setUser } = useContext(UserContext);
    const location = useLocation();
    
    function getCurrentPage() {
        return "/" + location.pathname.split('/')[1];
    };

    useEffect(() => {  
        fetchWithAuth('/api/user/me', {})
        .then(response => {
            return response.json();
        }).then(data => {        
            if (!data) {
                data = { anonymous : true };
            }
            setUser(data);      
        })
        .catch(error => {
            logError(error);
            setUser({ anonymous : true });
        });
    }, [setUser]);
    
    const callUserLogout = () => {
        fetch(`${API_URL}/api/user/logout`, {credentials: 'include'})
            .then(response => {
                if (response.ok) {
                    setUser(null);
                    navigate("/login");
                }
                response.text();
            })
            .catch(error => {
                logError(error);
                logError('Error occured during logout!');
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
            case '/watchMeTrade':
                navigate('/watchMeTrade');
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
                <h2 className="userEmail">{user.email}</h2> 
                <select id="navigation" form="navigation" className="select-dropdown" value={getCurrentPage()} onChange={handleSelectChange}>
                    <option value="/activity">Activity</option>
                    <option value="/travelGuide">Travel</option>
                    <option value="/movieGuide">Movies</option>
                    <option value="/bookGuide">Books</option>
                    <option value="/storage">Storage</option>
                    <option value="/feedback">Feedback</option>
                    <option value="/watchMeTrade">Trading</option>
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
