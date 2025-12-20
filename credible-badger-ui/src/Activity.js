import React, { useContext, useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';
import { FaStar, FaRegCalendarAlt } from "react-icons/fa";
import { fetchWithAuth } from './Api';

const Activity = () => {
    const { user } = useContext(UserContext);
    const { userId } = useParams();
    const [activities, setActivities] = useState([]);
    const [activityName, setActivityName] = useState('');
    const [activityCategory, setActivityCategory] = useState('PLACE');
    const [activityRating, setActivityRating] = useState('1');
    const [categoryFilter, setCategoryFilter] = useState('ALL');
    const [ratingFilter, setRatingFilter] = useState('1');
    const [yearFilter, setYearFilter] = useState(new Date().getFullYear());
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    const [editMarker, setEditMarker] = useState(null);
    const [editName, setEditName] = useState('');
    const [interactionMarker, setInteractionMarker] = useState(null);
    
    const [showNotification, setShowNotification] = useState(false);
    const [responseType, setResponseType] = useState([]);
    const [actionResponse, setActionResponse] = useState([]);
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown'
    };

    const retrieveActivities = useCallback(() => {
        const currentUserId = userId ? userId : (user ? user.id : 0);
        fetchWithAuth('/api/activity/retrieve', {
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
        .then(data => setActivities(data))
        .catch(error => {
            setActivities([]);
        });
    }, [user, apiUrl, ResponseTypes.ERROR_UNKNOWN]);
    
    useEffect(() => {
        if (!user) {
            return;
        }
        
        if (user.anonymous && !userId) {
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
        
        fetchWithAuth('/api/activity/submit', {
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

    const selectItemForInteraction = (item) => {
        if (item.id !== interactionMarker) {
            setEditMarker(null);   
            setInteractionMarker(item.id);
        }
    };    
    
    const selectItemForEdit = (item) => {
        setEditName(item.name);
        setEditMarker(item.id);
    };    

    const handleActivityNameEdit = (event) => {
        setEditName(event.target.value);
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

    const handleCategoryFilterChange = (event) => {
        setCategoryFilter(event.target.value);
    };
    
    const handleRatingFilterChange = (event) => {
        setRatingFilter(event.target.value);
    };
    
    const handleYearFilterChange = (event) => {
        setYearFilter(event.target.value);
    };

    const deleteActivity = (item) => {
        fetchWithAuth('/api/activity/delete', {
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
    
    const editItem = (item) => {
        item.name = editName;
        fetchWithAuth('/api/activity/update', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
        })
        .then(response => {
            if (response.ok) {
                setEditMarker(null);
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
        const parentFolder = userId ? "../" : "";
        switch (item) {
            case "PLACE":
              return parentFolder + "activity-place.jpg";
            case "MOVIE":
              return parentFolder + "activity-movie.jpg";
            case "GAME":
              return parentFolder + "activity-game.jpg";
            case "SHOW":
              return parentFolder + "activity-show.jpg";
            case "BOOK":
              return parentFolder + "activity-book.jpg";
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
    
    const getFilteredActivities = () => {
        return activities.filter(item => ((
            (categoryFilter === 'ALL' || item.category === categoryFilter) && 
            item.rating >= ratingFilter && 
            new Date(item.creationTime).getFullYear() === Number(yearFilter))));
    };
    
    const getFormattedNumberOfActivities = () => {
        const filteredActivities = getFilteredActivities().length;
        if (filteredActivities === 0) {
            return '';
        }
        return '(' + filteredActivities + ')';
    };
    
    const createYearFilterValues = () => {
        const currentYear = new Date().getFullYear();

        const years = Array.from(
          new Set(activities.map(item => new Date(item.creationTime).getFullYear()))
        );

        if (!years.includes(currentYear)) {
          years.push(currentYear);
        }

        years.sort((a, b) => b - a);  
        return years;
    };

    const goToSharePage = () => {
        navigate(`/activity/${user.id}`);
    };
    
    const goToActivityPage = () => {
        navigate("/activity");
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
                <p className="title">What did you do this year? Keep track of the places you have visited, books you have read, and movies you have watched!</p>
                {!userId &&
                <div className="activity-new">
                    <input type="text" placeholder="New Activity" id="activity-name" 
                           value={activityName} onChange={handleActivityNameChange}
                           className="activity-new-input"/>
                    <div>
                        <select className="activity-select" onChange={handleActivityCategoryChange} id="activity-category" >
                            <option value="PLACE">Place</option>
                            <option value="BOOK">Book</option>
                            <option value="MOVIE">Movie</option>
                            <option value="SHOW">Show</option>
                            <option value="GAME">Game</option>
                        </select>
                        <select className="activity-select" onChange={handleActivityRatingChange} id="activity-rating" >
                            <option value="1">1/5</option>
                            <option value="2">2/5</option>
                            <option value="3">3/5</option>
                            <option value="4">4/5</option>
                            <option value="5">5/5</option>
                        </select>
                        <button onClick={submitActivity}>Add</button>
                    </div>
                </div>}

                
                <div className="activities-header"> 
                    <span>Activities {getFormattedNumberOfActivities()}</span>
                    <div className="activity-filter-group">
                        <select className="activities-filter" id="activity-filter" 
                                onChange={handleCategoryFilterChange}>
                            <option value="ALL">All</option>
                            <option value="PLACE">Places</option>
                            <option value="BOOK">Books</option>
                            <option value="MOVIE">Movies</option>
                            <option value="SHOW">Shows</option>
                            <option value="GAME">Games</option>
                        </select>
                        <select className="activities-filter" id="activity-filter" 
                                onChange={handleRatingFilterChange}>
                            <option value="1">+1</option>
                            <option value="2">+2</option>
                            <option value="3">+3</option>
                            <option value="4">+4</option>
                            <option value="5">+5</option>
                        </select>
                        <select className="activities-filter" id="activity-filter" 
                                onChange={handleYearFilterChange}>   
                            {createYearFilterValues().map(year => (
                                <option value={year}>{year}</option>
                              ))}
                        </select>   
                    </div>
                </div>
                { activities.length > 0 &&
                <div> 
                    <ul className="simple-list">
                    { getFilteredActivities().map(item => (
                        <li key={item.id} className="simple-item" onClick={() => selectItemForInteraction(item)}>
                                { (editMarker !== item.id) ? (
                                <div className="item-content">
                                    <div className="image-container">
                                        <img className="activity-icon" src={getImageSource(item.category)} alt={item.category}/>
                                    </div>
                                    <span className="activity-name">
                                    {item.name}
                                    <div className={`list-item-actions ${!userId && editMarker !== item.id && interactionMarker === item.id ? 'visible' : 'hidden'}`}>
                                        { item.category === "MOVIE" && (
                                        <button className="" onClick={() => recommendMovies(item)}>Guide</button>
                                        )}
                                        { item.category === "BOOK" && (
                                        <button className="" onClick={() => recommendBooks(item)}>Guide</button>
                                        )}
                                        { item.category === "PLACE" && (
                                        <button className="" onClick={() => recommendTravel(item)}>Guide</button>
                                        )}
                                        <button className="" onClick={() => selectItemForEdit(item)}>Edit</button>
                                        <button className="" onClick={() => deleteActivity(item)}>Delete</button>
                                    </div>
                                    </span>
                                    <div className="activity-meta">{item.rating}/5&nbsp;<FaStar className="activity-meta-icon"/><br/>{formatDate(new Date(item.creationTime))}&nbsp;<FaRegCalendarAlt className="activity-meta-icon"/></div>  
                                </div>
                                ) : (
                                <div className="item-content">
                                    <button className="activity-edit-button" onClick={() => editItem(item)}>
                                        <img className="activity-store-icon" src="store-icon.jpg" alt="store item"/>
                                    </button>
                                    <input type="text" placeholder="New Activity" id="activity-name" 
                                           value={editName} onChange={handleActivityNameEdit}
                                           className="activity-edit-input"/>
                                </div>
                                )} 
                        </li>
                    ))}
                    </ul>
                </div>}
            </div>)}
            
            {activities.length === 0 && user && !user.anonymous && !userId &&
            <p>You haven't added anything yet!</p>
            }

            {activities.length === 0 && userId &&
            <p>This user hasn't shared any activities yet!</p>
            }
            
            <div className="content-margin">
                {activities.length !== 0 && user && !userId &&
                <button onClick={goToSharePage}>Share your activities!</button>
                }

                {user && !user.anonymous && userId &&
                <button onClick={goToActivityPage}>Manage your own activities!</button>
                }

                {(!user || user.anonymous) && userId &&
                <button onClick={goToActivityPage}>Sign up to track your own activities!</button>
                }
            </div>
            
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
