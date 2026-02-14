import React, { useContext, useState, useEffect } from "react";
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';
import { fetchWithAuth } from './Api';
import { logError } from './Logging';

const Feedback = () => {
    const { user } = useContext(UserContext);
    const [feedback, setFeedback] = useState([]);
    const [needHelp, setNeedHelp] = useState(false);
    const [interactionMarker, setInteractionMarker] = useState(null);
    const [archiveFilter, setArchiveFilter] = useState('false');
    
    const tutorialData = {
        projectKey : user ? user.id : "Loading...",
        projectVersion : "0.0.9-BETA",
        projectUser : "unknownUser",
        content : "I like the new patch!"
    };
    
    const retrieveFeedback = () => {
        fetchWithAuth('/api/feedback/retrieve', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
        })
        .then(response => {
                if (!response.ok) {
                    return [];
                }
                return response.json();
        })
        .then(data => setFeedback(data))
        .catch(error => {
            logError(error);
            setFeedback([]);
        });
    };
    
    useEffect(() => {
        retrieveFeedback();
    }, []);
    
    
    const updateFeedback = (item) => {
        fetchWithAuth('/api/feedback/update', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({id : item.id, archived : !item.archived})
        })
        .then(response => {
                if (!response.ok) {
                    return;
                }
                item.archived = !item.archived;
                setFeedback([...feedback]);
        })
        .catch(error => {
            logError(error);
            return;
        });
    };
    
    const deleteFeedback = (item) => {
        fetchWithAuth('/api/feedback/delete', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({id : item.id})
        })
        .then(response => {
            if (!response.ok) {
                return;
            }
            retrieveFeedback();
        })
        .catch(error => {
            logError(error);
            return;
        });
    };
    
    const toggleNeedHelp = () => {
        setNeedHelp(!needHelp);
    };
    
    const selectItemForInteraction = (item) => {
        if (item.id !== interactionMarker) {
            setInteractionMarker(item.id);
        }
    };
    
    const getFilteredFeedback = () => {
        return feedback.filter(item => ((archiveFilter === 'All' || '' + item.archived === archiveFilter)));
    };
    
        
    const handleFeedbackArchiveFilterChange = (event) => {
        setArchiveFilter(event.target.value);
    };
    
    const getFormattedFeedbackCount = () => {
        const filteredFeedback = getFilteredFeedback().length;
        if (filteredFeedback === 0) {
            return '';
        }
        return '(' + filteredFeedback + ')';
    };
    
    const getFilteredFeedbackMessage = () => {
        if ('' + archiveFilter === 'false') {
           return 'There is no new feedback';
        }
        if ('' + archiveFilter === 'true') {
           return 'There is no archived feedback';
        }
        return 'No feedback was submitted yet!';
    };
    
    
    return (
        <div className="content"> 
            <UserInfo />
            {!user ? (
                <p>Loading...</p>
            ) :
            (!user.emailVerified) ? (
                <p>Your email address is not verified! Please check your inbox or request a new verification email <a href="/verifyEmail">here</a>!</p>
            ) : 
            (
            <div>
                <p>
                    Send over your customer feedback with <a onClick={toggleNeedHelp}>a single REST call.</a> Manage it right here.
                </p>
                {needHelp &&
                <div>
                    <p>Send a POST request to https://crediblebadger.com/api/feedback/submit</p>
                    <pre className="json-code">{JSON.stringify(tutorialData, null, 2)}</pre>
                </div>
                }         
                
                 <div className="activities-header"> 
                    <span>Feedback {getFormattedFeedbackCount()}</span>
                    <div className="activity-filter-group">
                        <select value={archiveFilter} className="activity-select" onChange={handleFeedbackArchiveFilterChange} id="feedback-archive-filter" >
                            <option value="All">All</option>
                            <option value="false">New</option>
                            <option value="true">Archived</option>
                        </select>
                    </div>
                </div>
                { getFilteredFeedback().length === 0 && <p>{getFilteredFeedbackMessage()}</p> }
                
                <ul className="simple-list">
                {getFilteredFeedback().map(item => (
                    <li key={item.id} className="feedback-item" onClick={() => selectItemForInteraction(item)}>
                        {item.content}
                        <div className="feedback-footer">
                            {item.projectUser} | {item.projectVersion} | {item.creationTime.slice(0,16).replace('T', '-') }
                        </div>
                        <div className={`list-item-actions ${ interactionMarker === item.id ? 'visible' : 'hidden'}`}>
                            <button className="" onClick={() => updateFeedback(item)}>Archive</button>
                            <button className="" onClick={() => deleteFeedback(item)}>Delete</button>
                        </div>
                    </li>
                ))}
                </ul>
            </div>    
            )}                                    
            <div className="footer">
                <Footer/>
            </div>
        </div>
        );
};

export default Feedback;
