import React, { useCallback, useContext, useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';
import { FaStar, FaRegCalendarAlt, FaDownload, FaTrash} from "react-icons/fa";
import { fetchWithAuth } from './Api';
import { logError } from './Logging';

const Notes = () => {
    const { user } = useContext(UserContext);
    const { userId } = useParams();
    const [notes, setNotes] = useState([]);
    const [noteDescription, setNoteDescription] = useState('');
    const [noteFilter, setNoteFilter] = useState('');
    const navigate = useNavigate();
    const [editMarker, setEditMarker] = useState(null);
    const [editName, setEditName] = useState('');
    const [interactionMarker, setInteractionMarker] = useState(null);
    const [userFiles, setUserFiles] = useState([]);
    const [dragTarget, setDragTarget] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState(false);
    const [selectedPreview, setSelectedPreview] = useState(null);
    
    const [showNotification, setShowNotification] = useState(false);
    const [responseType, setResponseType] = useState([]);
    const [actionResponse, setActionResponse] = useState([]);
    const [usedStorage, setUsedStorage] = useState(null);
    const [totalStorage, setTotalStorage] = useState(null);
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown'
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
    
    const retrieveUserFiles = useCallback(() => {
        fetchWithAuth('/api/storage/retrieveUserFiles', {
            method: 'GET',
            credentials: 'include'})
            .then(response => { 
                if(response.ok) {
                    return response.json();
                }
                else {
                    displayActionResponse("Retrieving files failed: " + response.status, false);
                }
                return null;
            })
            .then(data => {
                if (data !== null) {
                    setTotalStorage(data.totalUserSpace);
                    setUsedStorage(data.usedUserSpace);
                    setUserFiles(data.userFiles);   
                }
            });
    }, []);

    const retrieveNotes = useCallback(() => {
        const currentUserId = userId ? userId : (user ? user.id : 0);
        fetchWithAuth('/api/notes/retrieve', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId : currentUserId})
        })
        .then(response => {
                if (!response.ok) {
                    displayActionResponse("Data could not be retrieved!", ResponseTypes.ERROR_UNKNOWN);
                    return [];
                }
                return response.json();
        })
        .then(data => setNotes(data))
        .catch(error => {
            logError(error);
            setNotes([]);
        });
    }, [ResponseTypes.ERROR_UNKNOWN, user, userId]);
    
    useEffect(() => {
        if (!user) {
            return;
        }
        
        if (user.anonymous && !userId) {
            navigate('/login');
            return;
        }
        retrieveNotes();
        retrieveUserFiles();
    }, [user, userId, retrieveNotes, retrieveUserFiles, navigate]);
    
    const submitNote = () => {
        
        if (!noteDescription || noteDescription.trim().length === 0) {
            displayActionResponse("Please enter an note name!", ResponseTypes.ERROR_UNKNOWN);
            return;
        }
        
        fetchWithAuth('/api/notes/submit', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({description: noteDescription})
        })
        .then(response => {
            if (response.ok) {
                retrieveNotes();
                setNoteDescription('');
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
            }
        })    
        .catch(error => {
            logError(error);
        });
    };

    const selectItemForInteraction = (item) => {
        if (item.id === interactionMarker) {
            return;
        }
        setInteractionMarker(item.id);
        setEditMarker(null);   
        setDragTarget(null);
        setDeleteTarget(null);
    };    
    
    const selectItemForEdit = (item) => {
        setEditName(item.description);
        setEditMarker(item.id);
    };    

    const handleActivityNameEdit = (event) => {
        setEditName(event.target.value);
        event.target.style.height = 'auto';        
        const calculatedHeight = event.target.scrollHeight - 20;
        event.target.style.height = calculatedHeight + 'px';
    };
    
    const handleNoteNameChange = (event) => {
        setNoteDescription(event.target.value);
        event.target.style.height = 'auto';        
        const calculatedHeight = event.target.scrollHeight - 20;
        event.target.style.height = calculatedHeight + 'px';
    };
    
    const handleNoteFilterChange = (event) => {
        setNoteFilter(event.target.value);
    };
        
    const fileInputRef = useRef(null);

    const handleFileInputChange = (e) => {
        const files = Array.from(e.target.files);
        files.forEach(file => uploadFile(file));
    };
    
    const uploadFile = (file, id) => {
        const formDataUpload = new FormData();
        formDataUpload.append('data', file);
        const prefix = id ? id : interactionMarker;
        formDataUpload.append("file_name", prefix + "-" + file.name);
        
        fetchWithAuth(`/api/storage/uploadFile`, {
            method: 'POST',
            credentials: 'include',
            body: formDataUpload
        })
        .then(response => { 
            if(response.ok) {
                retrieveUserFiles();
                displayActionResponse("Upload complete!", ResponseTypes.SUCCESS);                
            }
            else {
                displayActionResponse("Upload failed: " + response.status, false);
            }
        })
        .catch(error => {
            displayActionResponse("Upload failed: " + error.message, false);
        });
    };

    const deleteNote = (item) => {
        fetchWithAuth('/api/notes/delete', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
        })
        .then(response => {
            if (response.ok) {
                retrieveNotes();
                retrieveUserFiles();
                return response.json();
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })  
        .catch(error => {
            logError(error);
        });
    };
    
    const editItem = (item) => {
        item.description = editName;
        fetchWithAuth('/api/notes/update', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
        })
        .then(response => {
            if (!response.ok) {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return;
            }
            setEditMarker(null);
            retrieveNotes();
        })     
        .catch(error => {
            logError(error);
        });
    };

    const formatDate = (timestamp) => {
        const date = new Date(timestamp);
        const month = date.getMonth() + 1; // Months are zero-indexed
        const day = date.getDate();
        const year = date.getFullYear().toString().slice(-2); // Get the last two digits of the year
        return `${month}/${day}/${year}`;
    };
    
    const getFilteredNotes = () => {
        const words = noteFilter.toLowerCase().split(/\s+/).filter(w => w.length > 0);
        return notes.filter(item => words.every(word => item.description.toLowerCase().includes(word)));
    };
    
    const getFormattedNumberOfNotes = () => {
        const filteredActivities = getFilteredNotes().length;
        if (filteredActivities === 0) {
            return '';
        }
        return '(' + filteredActivities + ')';
    };
    
    const formatBytes = (bytes) => {
      if (bytes === 0) return '0 B';
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(bytes) / Math.log(1024));
      const value = bytes / Math.pow(1024, i);
      return `${value % 1 === 0 ? value : value.toFixed(2)} ${sizes[i]}`;
    };
    
    const getFormattedConsumption = () => {
    if (!totalStorage) {
        return ' - Loading Files...';
    }
      return ` using ${formatBytes(usedStorage)} / ${formatBytes(totalStorage)}`;
    };

    const getFilteredUserFiles = (prefix) => {
        return userFiles.filter(item => (item.startsWith(prefix)));
    };

    const handleDownload = (e, prefix, file) => {
        e.stopPropagation();
        downloadFile(prefix, file);
    };
    
    const downloadFile = (prefix, filename_local) => {
       const formDataDownload = new FormData();
       formDataDownload.append("file_name", filename_local);
        
        fetchWithAuth('/api/storage/downloadFile', {
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
            a.download = filename_local.substring(prefix.length + 1);
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        });   
    };
    
    const handleDeleteFile = (e, file) => {
        e.stopPropagation();
        deleteFile(file);
    };

    const handleDeleteNote = (item) => {
        setDeleteTarget(item);
    };
    
    const confirmDeleteNote = (e) => {
        e.stopPropagation();
        deleteNote(deleteTarget);
        setDeleteTarget(null);
    };
    
    const cancelDeleteNote = (e) => {
        e.stopPropagation();
        setDeleteTarget(null);
    };
    
    const deleteFile = (fileName_local) => {
       const formDataDelete = new FormData();
       formDataDelete.append("file_name", fileName_local);
        
        fetchWithAuth('/api/storage/deleteFile', {
            method: 'POST',
            credentials: 'include',
        body: formDataDelete
        })
        .then(response => { 
            retrieveUserFiles();
            if(response.ok) {
                displayActionResponse("File was deleted!", ResponseTypes.SUCCESS);                
            }
            else {
                displayActionResponse("File deletion failed: " + response.status, false);
            }
        });
    };
    
    const handleDragOver = (e, id) => {
        e.preventDefault();
        setDragTarget(id);
    };
    
    const handleDragLeave = (e) => {
        // prevents overlay from flickering
        if (e.currentTarget.contains(e.relatedTarget)) {
            return;
        }
        e.preventDefault();
        setDragTarget(null);
    };

    const handleDrop = (e, id) => {
      e.preventDefault();
      setDragTarget(null);
      setInteractionMarker(id);
      const files = Array.from(e.dataTransfer.files);
      files.forEach(file => uploadFile(file, id));
    };
    
    const IMAGE_EXTENSIONS = new Set([
        ".jpg", ".jpeg", ".png", ".gif", ".webp",
        ".avif", ".svg", ".bmp", ".tiff", ".tif", ".ico"
    ]);

    const showPreview = (prefix, filename_local) => {
        const ext = filename_local.slice(filename_local.lastIndexOf('.')).toLowerCase();
        if (!IMAGE_EXTENSIONS.has(ext)) {
            return;
        }
        
       const formDataDownload = new FormData();
       formDataDownload.append("file_name", filename_local);
        
        fetchWithAuth('/api/storage/downloadFile', {
            method: 'POST',
            credentials: 'include',
        body: formDataDownload
        })
        .then(response => {
            return response.blob(); })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            setSelectedPreview(url);
        });   
    };
    
    const closePreview = () => {
        if (selectedPreview) {
            window.URL.revokeObjectURL(selectedPreview);
        }
        setSelectedPreview(null);
    };

    return (
        <div className="content"> 
            <UserInfo />
            {!user ? (
                <p>Loading...</p>
            ) :
            (!user.emailVerified && !userId) ? (
                <p>Your email address is not verified! Please check your inbox or request a new verification email <a href="/verifyEmail">here</a>!</p>
            ) : 
            (
            <div>
                <p className="title">Store and Manage your Notes Online!</p>
                {!userId &&
                <div className="activity-new">
                    <textarea placeholder="New Note" id="note-name" value={noteDescription} onChange={handleNoteNameChange} className="activity-new-input" rows={1}/>
                    <div>
                        <button onClick={submitNote}>Add</button>
                    </div>
                </div>}
                
                {notes.length === 0 && user && !user.anonymous && !userId ? (
                <p>You haven't added anything yet!</p>
                ) :
                (
                <div className="notes-header"> 
                    <span>Notes {getFormattedNumberOfNotes()}{getFormattedConsumption()}</span>
                    <div className="search-note-container">
                        <input type="text" placeholder="Search For Note" id="activity-name" value={noteFilter} onChange={handleNoteFilterChange} className="search-note-input"/>
                    </div>
                </div>
                )}
                
                { selectedPreview &&
                <div className="overlay-backdrop" onClick={closePreview}>
                    <img
                      className="overlay-img"
                      src={selectedPreview}
                    />
                </div>
                }
                { notes.length > 0 &&
                <div> 
                    <ul className="simple-list">
                    { getFilteredNotes().map(item => (
                        <li key={item.id} className={`simple-item ${interactionMarker === item.id ? 'marked-for-interaction' : ''}`} onClick={() => selectItemForInteraction(item)}>
                                { (editMarker !== item.id) ? (
                                <div className="note-content" onDragOver={(e) => handleDragOver(e, item.id)} onDragLeave={handleDragLeave} onDrop={(e) => handleDrop(e, item.id)}>
                                    {item.id === dragTarget && (
                                      <div className="drop-overlay">
                                        <span>Drop your files here</span>
                                      </div>
                                    )}
                                    {item === deleteTarget && (
                                      <div className="delete-overlay">
                                        <span>Delete this note?</span>
                                        <div  className="list-item-actions wide-gap">
                                            <button className="" onClick={(e) => confirmDeleteNote(e)}>Confirm</button>
                                            <button className="" onClick={(e) => cancelDeleteNote(e)}>Cancel</button>
                                        </div>
                                      </div>
                                    )}
                                    <div className="activity-name">{item.description}</div>
                                    <div className={`note-file-container ${!userId && editMarker !== item.id && interactionMarker === item.id ? 'visible' : 'hidden'}`}>
                                    { getFilteredUserFiles(item.id).map(file => (
                                    <div onClick={() => showPreview(item.id, file)} key={file} className="note-file">
                                      <span className="note-file-row">
                                        <span className="note-file-name" title={file.substring(item.id.length + 1)}>
                                          {file.substring(item.id.length + 1)}
                                        </span>
                                        <span className="note-file-actions">
                                          <span onClick={(e) => handleDownload(e, item.id, file)}>
                                            <FaDownload size={15} />
                                          </span>
                                          <span onClick={(e) => handleDeleteFile(e, file)}>
                                            <FaTrash size={15} />
                                          </span>
                                        </span>
                                      </span>
                                    </div>
                                    ))}
                                    </div>
                                    <div className={`list-item-actions ${!userId && editMarker !== item.id && interactionMarker === item.id ? 'visible' : 'hidden'}`}>
                                        <button className="" onClick={() => selectItemForEdit(item)}>Edit</button>
                                        <button className="" onClick={() => handleDeleteNote(item)}>Delete</button>
                                        <input type="file" ref={fileInputRef} onChange={handleFileInputChange} onInput={handleFileInputChange} style={{ display: 'none' }} multiple/>
                                        <button className="" onClick={() => fileInputRef.current.click()}>Upload Files</button>
                                        <div className="activity-meta">{formatDate(new Date(item.lastModified))}&nbsp;<FaRegCalendarAlt className="activity-meta-icon"/></div>  
                                    </div>
                                </div>
                                ) : (
                                <div className="item-content">
                                    <button className="activity-edit-button" onClick={() => editItem(item)}>
                                        <img className="activity-store-icon" src="store-icon.jpg" alt="store item"/>
                                    </button>
                                    <textarea placeholder="New Note" id="activity-name" 
                                           value={editName} onChange={handleActivityNameEdit}
                                           className="activity-edit-input"/>
                                </div>
                                )} 
                        </li>
                    ))}
                    </ul>
                </div>}
            </div>)}

            {notes.length === 0 && userId &&
            <p>This user hasn't shared any notes yet!</p>
            }
            
            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
                                
            <div className="footer">
                <Footer/>
            </div>
        </div>
        );
};

export default Notes;
