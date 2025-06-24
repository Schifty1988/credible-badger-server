import React, { useContext, useState, useEffect, useCallback } from "react";
import { useNavigate } from 'react-router-dom';
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';

const Activity = () => {
    const { user } = useContext(UserContext);
    const [activities, setActivities] = useState([]);
    const [activityName, setActivityName] = useState('');
    const [activityCategory, setActivityCategory] = useState('PLACE');
    const [activityRating, setActivityRating] = useState('5');
    const [activityFilter, setActivityFilter] = useState('ALL');
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    
    const [showNotification, setShowNotification] = useState(false);
    const [responseType, setResponseType] = useState([]);
    const [actionResponse, setActionResponse] = useState([]);
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown'
    };

    const retrieveActivities = useCallback(() => {
        fetch(`${apiUrl}/api/activity/retrieve`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
        })
        .then(response => {
                if (!response.ok) {
                    displayActionResponse("Data could not be retrieved!", ResponseTypes.ERROR_UNKNOWN);
                    return [];
                }
                return response.json();
        })
        .then(data => setActivities(data))
        .catch(error => {
            setActivities([]);
        });
    }, [apiUrl, ResponseTypes.ERROR_UNKNOWN]);
    
    useEffect(() => {
        if (!user) {
            return;
        }
        
        if (user.anonymous) {
            navigate('/login');
            return;
        }
        retrieveActivities();
    }, [user, retrieveActivities, navigate]);
    
    const submitActivity = () => {
        
        if (!activityName || activityName.trim().length === 0) {
            displayActionResponse("Please enter an activity name!", ResponseTypes.ERROR_UNKNOWN);
            return;
        }
        
        fetch(`${apiUrl}/api/activity/submit`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name: activityName, category: activityCategory, rating: activityRating})
        })
        .then(response => {
            if (response.ok) {
                retrieveActivities();
                setActivityName('');
                return response.json();
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })
        .then(data => {
        })      
        .catch(error => {
            
        });
    };
    
    const handleActivityNameChange = (event) => {
        setActivityName(event.target.value);
    };
    
    const handleActivityCategoryChange = (event) => {
        setActivityCategory(event.target.value);
    };
    
    const handleActivityRatingChange = (event) => {
        setActivityRating(event.target.value);
    };

    const handleActivityFilterChange = (event) => {
        setActivityFilter(event.target.value);
    };

    const deleteActivity = (item) => {
        fetch(`${apiUrl}/api/activity/delete`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
        })
        .then(response => {
            if (response.ok) {
                retrieveActivities();
                return response.json();
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })
        .then(data => {    
        })      
        .catch(error => {
        });
    };
    
    const recommendMovies = (item) => {
        const guideRequest = JSON.stringify({name: item.name});   
        const encodedGuideRequest = btoa(guideRequest);
        navigate('/movieGuide/' + encodedGuideRequest);
    };
    
    const recommendBooks = (item) => {
        const guideRequest = JSON.stringify({name: item.name});   
        const encodedGuideRequest = btoa(guideRequest);
        navigate('/bookGuide/' + encodedGuideRequest);
    };
    
    const recommendTravel = (item) => {
        const guideRequest = JSON.stringify({place: item.name, childFriendly: true});   
        const encodedGuideRequest = btoa(guideRequest);
        navigate('/travelGuide/' + encodedGuideRequest);
    };
    
    const getImageSource = (item) => {
        switch (item) {
            case "PLACE":
              return "activity-place.jpg";
            case "MOVIE":
              return "activity-movie.jpg";
            case "GAME":
              return "activity-game.jpg";
            case "SHOW":
              return "activity-show.jpg";
            case "BOOK":
              return "activity-book.jpg";
            default:
              return "";
        }
    };

    const formatDate = (timestamp) => {
        const date = new Date(timestamp);
        const month = date.getMonth() + 1; // Months are zero-indexed
        const day = date.getDate();
        const year = date.getFullYear().toString().slice(-2); // Get the last two digits of the year
        return `${month}/${day}/${year}`;
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
                <p className="title">What did you do in 2025? Keep track of the places you have visited, books you have read, and movies you have watched!</p>
                
                <div className="activity-new">
                    <input type="text" placeholder="New Activity" id="activity-name" 
                           value={activityName} onChange={handleActivityNameChange}
                           className="activity-new-input"/>
                            <div>
                                <select className="activity-select" onChange={handleActivityRatingChange} id="activity-rating" >
                                    <option value="1">1/5</option>
                                    <option value="2">2/5</option>
                                    <option value="3">3/5</option>
                                    <option value="4">4/5</option>
                                    <option value="5">5/5</option>
                                </select>
                                <select className="activity-select" onChange={handleActivityCategoryChange} id="activity-category" >
                                    <option value="PLACE">Place</option>
                                    <option value="BOOK">Book</option>
                                    <option value="MOVIE">Movie</option>
                                    <option value="SHOW">Show</option>
                                    <option value="GAME">Game</option>
                                </select>
                                <button onClick={submitActivity}>Add</button>
                           </div>
                </div>
                
                <div className="activities-header"> 
                <span>Your Activities</span>
                    <select className="activities-filter" id="activity-filter" 
                            onChange={handleActivityFilterChange}>
                        <option value="ALL">All</option>
                        <option value="PLACE">Place</option>
                        <option value="BOOK">Book</option>
                        <option value="MOVIE">Movie</option>
                        <option value="SHOW">Show</option>
                        <option value="GAME">Game</option>
                    </select>    
                </div>
                { activities.length > 0 ? (
                <div> 
                    <ul className="simple-list">
                    { activities.map(item => (
                        (activityFilter === 'ALL' || item.category === activityFilter) &&
                        <li key={item.id} className="simple-item">
                                <img className="activity-icon" src={getImageSource(item.category)} alt="item.category"/>
                                <span className="activity-name">{item.name}</span>
                                <span className="activity-meta">{item.rating}/5<br/>{formatDate(new Date(item.creationTime))}</span>  
                                <div className="list-item-actions">
                                    { item.category === "MOVIE" && (
                                    <button className="green-button" onClick={() => recommendMovies(item)}>Recommendations</button>
                                    )}
                                    { item.category === "BOOK" && (
                                    <button className="green-button" onClick={() => recommendBooks(item)}>Recommendations</button>
                                    )}
                                    { item.category === "PLACE" && (
                                    <button className="green-button" onClick={() => recommendTravel(item)}>Recommendations</button>
                                    )}
                                    <button className="red-button" onClick={() => deleteActivity(item)}>Delete</button>
                                </div>
                        </li>
                    ))}
                    </ul>
                </div>
                ) : (
                <p>You haven't added anything yet!</p>
                )}
            </div>)}
            
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

export default Activity;
