import './App.css';
import React, { useContext, useState } from "react";
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';

const Admin = () => {
    const [actionResponse, setActionResponse] = useState([]); 
    const [responseType, setResponseType] = useState([]); 
    const [travelGuideId, setTravelGuideId] = useState([]); 
    const [users, setUsers] = useState([]);
    const [storageInfo, setStorageInfo] = useState([]);
    const { user } = useContext(UserContext);
    const apiUrl = process.env.REACT_APP_API_URL;
    const [showNotification, setShowNotification] = useState(false);

    const listUsers = () => {
        fetch(`${apiUrl}/api/admin/listUsers`, {
            method: 'GET',
            credentials: 'include'})
            .then(response => {
                if(response.ok) {
                    displayActionResponse("Users retrieved!", true);
                    return response.json();
                }
                displayActionResponse("An error occured retrieving the list of users: " + response.status, false);
                return [];
            })
            .then(data => {
                setUsers(data);
            });
    };
    
    const retrieveStorageInfo = () => {
        fetch(`${apiUrl}/api/admin/storageInfo`, {
            method: 'GET',
            credentials: 'include'})
            .then(response => {
                if (response.ok) {
                    displayActionResponse("Storage info retrieved!", true);
                }
                else {
                    displayActionResponse("Failed to retrieve storage info: " + response.status, false);
                }
                return response.json();
            }).then(data => {
                setStorageInfo(data);
            });
    };
    
    const suspendUser = (userId, suspended) => {
        fetch(`${apiUrl}/api/admin/suspendUser`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({userId: userId, suspended : suspended}) 
        })
        .then(response => { 
            if (response.ok) {
                displayActionResponse("Update successful!", true);
                const updatedUsers = users.map(user =>
                    user.id === userId ? { ...user, suspended: suspended } : user
                );
                setUsers(updatedUsers);
            }
            else {
                displayActionResponse("Failed to change suspension status: " + response.status, false);
            }
        });
    };
    
    const isAdmin = () => {
        const adminRole = user.roles.find(role => role.role === 'ROLE_ADMIN');
        return adminRole !== undefined;
    };
    
    const displayActionResponse = (message, responseType) => {
        setResponseType(responseType);
        setActionResponse(message);

        if (message.length > 0) {
            setShowNotification(true);
            setTimeout(() => {
                setShowNotification(false);
            }, 3000); 
        }
    };
    
    const handleTravelGuideIdChange = (event) => {
        setTravelGuideId(event.target.value);
    };
    
    const deleteTravelGuide = (userId, suspended) => {
        fetch(`${apiUrl}/api/admin/guide/` + travelGuideId, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(response => { 
            if (response.ok) {
                displayActionResponse("Guide Removed!", true);
                const updatedUsers = users.map(user =>
                    user.id === userId ? { ...user, suspended: suspended } : user
                );
                setUsers(updatedUsers);
            }
            else {
                displayActionResponse("Failed to Remove Guide: " + response.status, false);
            }
        });
    };

    return (
        <div className="content">
            <UserInfo />

            {!user ? (
                <p>Loading...</p>
            ) :
            (!isAdmin()) ? (
                <p>User does not have permissions to view this page!</p>
            ) : 
            (
                <div className="content-group">
                    <h2>Admin Controls</h2>
                        <button type="button" onClick={listUsers}>List Users</button>
                        
                        <ul className="simple-list">
                        {users.map(item => (
                            <li key={item.id} className="simple-item">
                                <span className="list-item-name">{item.email} | {item.createdAt.slice(0,16).replace('T', '-')}</span>
                                <div className="list-item-actions">
                                {item.suspended ? (
                                    <button className="green-button" onClick={() => suspendUser(item.id, false)}>Unsuspend</button>
                                ) : (
                                    <button className="red-button" onClick={() => suspendUser(item.id, true)}>Suspend</button>
                                )}
                                </div>
                            </li>
                        ))}
                        </ul>
                        
                    <button type="button" onClick={retrieveStorageInfo}>Storage Info</button>
                    
                    <ul className="simple-list">
                        {Object.entries(storageInfo).map(([key, value]) => (
                        <li key={key} className="key-value-item">
                            <div className="key-column">{key}</div>
                            <div className="value-column">{value}</div>
                        </li>
                        ))}
                    </ul>
                    
                    
                    <div className="content-group">
                        <input type="text" placeholder="Travel Guide Id" id="travelGuideId" value={travelGuideId} onChange={handleTravelGuideIdChange}/>
                        <button type="button" onClick={deleteTravelGuide}>Delete Guide</button>
                    </div>
                {showNotification && (
                    <div className={responseType ? "notification-success" : "notification-error"}>
                        {actionResponse} 
                    </div>
                )}
                </div>
            )}
        </div>
    );
};

export default Admin;
