import './App.css';
import React, { useContext, useState } from "react";
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';

const Admin = () => {
    const [actionResponse, setActionResponse] = useState([]); 
    const [responseType, setResponseType] = useState([]); 
    const [users, setUsers] = useState([]);
    const [storageInfo, setStorageInfo] = useState([]);
    const { user } = useContext(UserContext);
    const apiUrl = process.env.REACT_APP_API_URL;

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
    
    const displayActionResponse = (message, wasSuccessful) => {
        setResponseType(wasSuccessful ? "success" : "error");
        setActionResponse(message);
    };

    return (
        <div className="Content">
            <UserInfo />

            {!user ? (
                <p>Loading...</p>
            ) :
            (!isAdmin()) ? (
                <p>User does not have permissions to view this page!</p>
            ) : 
            (
                <div>
                    <h2>Admin Controls</h2>
                    <div className="content-group">
                        <button type="button" onClick={listUsers}>List Users</button>
                        <button type="button" onClick={retrieveStorageInfo}>Storage Info</button>
                    </div>

                    <ul className="simple-list">
                        {users.map(item => (
                            <li key={item.id} className="simple-item">
                                <span className="list-item-name">{item.email}</span>
                                <div className="list-item-actions">
                                {item.suspended ? (
                                    <button className="list-item-button download-button" onClick={() => suspendUser(item.id, false)}>Unsuspend</button>
                                ) : (
                                    <button className="list-item-button delete-button" onClick={() => suspendUser(item.id, true)}>Suspend</button>
                                )}
                                </div>
                            </li>
                        ))}
                    </ul>
                    
                    <ul className="simple-list">
                        {Object.entries(storageInfo).map(([key, value]) => (
                        <li key={key} className="key-value-item">
                            <div className="key-column">{key}</div>
                            <div className="value-column">{value}</div>
                        </li>
                        ))}
                    </ul>
                    <p className={responseType}>{actionResponse}</p>
                </div>
            )}
        </div>
    );
};

export default Admin;
