import React, { useContext, useEffect, useState } from "react";
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';

const Storage = () => {
    const [actionResponse, setActionResponse] = useState([]); 
    const [responseType, setResponseType] = useState([]); 
    const [file, setFile] = useState(null);
    const [isUploadDisabled, setUploadDisabled] = useState(true);
    const [userFiles, setUserFiles] = useState([]);
    const { user } = useContext(UserContext);
    const apiUrl = process.env.REACT_APP_API_URL;

    useEffect(() => {
        if (user && user.emailVerified) {
           retrieveUserFiles();             
        }
    }, [user]);
    
    const retrieveUserFiles = () => {
        fetch(`${apiUrl}/api/storage/retrieveUserFiles`, {
            method: 'GET',
            credentials: 'include'})
            .then(response => { 
                if(response.ok) {
                    return response.json();
                }
                else {
                    displayActionResponse("Retrieving files failed: " + response.status, false);
                }
                return [];
            })
            .then(data => setUserFiles(data));
    };
    
    const handleFileChange = (event) => {
        const selectedFile = event.target.files[0]; 
        setFile(selectedFile);
        setUploadDisabled(!selectedFile);
    };
    
    const uploadFile = () => {
        const formDataUpload = new FormData();
        formDataUpload.append('data', file);
        formDataUpload.append("file_name", file.name);
        
        fetch(`${apiUrl}/api/storage/uploadFile`, {
            method: 'POST',
            credentials: 'include',
            body: formDataUpload
        })
        .then(response => { 
            retrieveUserFiles();
            if(response.ok) {
                displayActionResponse("Upload complete!", true);                
            }
            else {
                displayActionResponse("Upload failed: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Upload failed: " + error.message, false);
        });
    };
    
    const downloadFile = (filename_local) => {
       const formDataDownload = new FormData();
       formDataDownload.append("file_name", filename_local);
        
        fetch(`${apiUrl}/api/storage/downloadFile`, {
            method: 'POST',
            credentials: 'include',
        body: formDataDownload
        })
        .then(response => {
            return response.blob(); })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename_local;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        });
    
    };
    
    const deleteFile = (fileName_local) => {
       const formDataDelete = new FormData();
       formDataDelete.append("file_name", fileName_local);
        
        fetch(`${apiUrl}/api/storage/deleteFile`, {
            method: 'POST',
            credentials: 'include',
        body: formDataDelete
        })
        .then(response => { 
            retrieveUserFiles();
            if(response.ok) {
                displayActionResponse("File was deleted!", true);                
            }
            else {
                displayActionResponse("File deletion failed: " + response.status, false);
            }
        });
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
            (!user.emailVerified) ? (
                <p>Your email address is not verified! Please check your inbox or request a new verification email <a href="/verifyEmail">here</a>!</p>
            ) : 
            (
                <div className="storage">
                    <h2>Your Files</h2>
                    <div className="list-item-actions">
                        <button className="list-item-button download-button" onClick={() => retrieveUserFiles()}>Retrieve User Files</button>
                    </div>

                    <ul className="simple-list">
                        {userFiles.map(item => (
                            <li key={item} className="simple-item">
                                <span className="list-item-name">{item}</span>
                                <div className="list-item-actions">
                                    <button className="list-item-button download-button" onClick={() => downloadFile(item)}>Download</button>
                                    <button className="list-item-button delete-button" onClick={() => deleteFile(item)}>Delete</button>
                                </div>
                            </li>
                        ))}
                    </ul>

                    <div className="content-group">
                        <input type="file" onChange={handleFileChange} />
                        <button type='button' onClick={() => uploadFile()} disabled={isUploadDisabled}>Upload File</button>
                    </div>
                </div>
            )}
            <p className={responseType}>{actionResponse}</p>
        </div>
        );
};

export default Storage;
