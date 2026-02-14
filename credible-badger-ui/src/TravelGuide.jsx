import './App.css';
import React, { useCallback, useContext, useState, useEffect } from "react";
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';
import { fetchWithAuth } from './Api';
import { FaHeart, FaRegHeart } from 'react-icons/fa';
import { API_URL } from './Api';
import { logError } from './Logging';

const TravelGuide = () => {
    const { guideLink } = useParams();
    const [readyForSearch, setReadyForSearch] = useState(false);
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const [place, setPlace] = useState("");
    const [loading, setLoading] = useState(false);
    const [childFriendly, setChildFriendly] = useState(true);
    const [recommendations, setRecommendations] = useState([]);
    const navigate = useNavigate();
    const location = useLocation();
    const [state] = useState(() => location.state || {});
    const [showNotification, setShowNotification] = useState(false);
    const isMobile = /Android|iPhone|iPad|iPod|Opera Mini|IEMobile|WPDesktop/i.test(navigator.userAgent);
    const { user } = useContext(UserContext);

    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown',
        ERROR_PLACE: 'error_place'
    };
    
    const hasGuideLink = useCallback(() => {
        return guideLink && guideLink.length > 0;
    }, [guideLink]);
    
    const resolveGuideLink = useCallback(() => {
        const jsonGuideLink = atob(guideLink);
        const decodedJson = JSON.parse(jsonGuideLink);
        setPlace(decodedJson.place);
        setChildFriendly(decodedJson.childFriendly);
    }, [guideLink]);
    
    const copyGuideLink = () => {
        const guideRequest = JSON.stringify({place: place, childFriendly: childFriendly});   
        const encodedGuideRequest = btoa(guideRequest);
        const link  = API_URL + "/travelGuide/" + encodedGuideRequest;
        navigator.clipboard.writeText(link);
        
        if (!isMobile) {
            displayActionResponse("Link was copied!", ResponseTypes.SUCCESS);   
        }
    };
    
    const validateInput = useCallback(() => {
        if (place.length < 2) {
            displayActionResponse("The provided place seems invalid.", ResponseTypes.ERROR_PLACE);
            return false;
        }   
        return true;
    }, [place, ResponseTypes.ERROR_PLACE]);    

    const fetchStream = useCallback(async () => {
        if (!validateInput()) {
            return;
        }  
        displayActionResponse("", ResponseTypes.SUCCESS);
        setLoading(true);
        
        const controller = new AbortController();

        const tr = Array.from({ length: 20 }, (_, i) => ({
            id: i + 1,
            name: "Recommendation Title",
            description: "Description Description Description Description Description Description"}));

        const response = await fetchWithAuth('/api/recommendation/travel', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"},
            body: JSON.stringify({place: place, childFriendly: childFriendly}),
            signal: controller.signal
        })
        .catch(error => {
            displayActionResponse("Guide creation failed: " + error.message, ResponseTypes.ERROR_UNKNOWN);
            setLoading(false);
        });

        if (!response.body)
            return;

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        let buffer = ""; // keep partial data
        let index = 0;

        async function readChunk() {
            const {done, value} = await reader.read();
            if (done) {
                displayActionResponse("Guide was created!", ResponseTypes.SUCCESS);
                setLoading(false);
                setRecommendations(tr.filter((_, i) => tr[i].loaded));
                return;
            }
            buffer += decoder.decode(value, {stream: true});
            
            const lines = buffer.split("\n");
            buffer = lines.pop();

            for (const line of lines) {
                if (!line.trim())
                    continue;
                try {
                    const json = JSON.parse(line.slice(5)); // remove "data:"
                    json.loaded = true;
                    tr[index] = json;
                    setRecommendations([...tr]);
                    ++index;
                } catch (err) {
                    logError(err);
                    displayActionResponse("Guide creation failed: " + response.status, ResponseTypes.ERROR_UNKNOWN);
                }
            }

            readChunk(); // recursively read next chunk
        }

        readChunk();
    }, [ResponseTypes.ERROR_UNKNOWN, ResponseTypes.SUCCESS, childFriendly, place, validateInput]);
    
     const createRecommendationStream = useCallback(() => {
         fetchStream().catch(err => logError(err));
     }, [fetchStream]);
     
    const likeRecommendation = (item) => {
        if (!user || user.anonymous) {
            displayActionResponse("Please log in to like this!", ResponseTypes.ERROR_UNKNOWN);
            return;
        }
        fetchWithAuth('/api/recommendation/like', {
            method: 'POST',
            body: JSON.stringify({recommendationId: item.id}),
            headers: {
            'Content-Type': 'application/json'},
            credentials: 'include'})
            .then(response => {
                if(response.ok) {
                    displayActionResponse("Liked!", ResponseTypes.SUCCESS);
                    item.likes = item.likes + 1;
                    item.likedByUser = true;
                    return;
                }
                displayActionResponse("An error occured: " + response.status, ResponseTypes.ERROR_UNKNOWN);
                return;
            });
    };
    
        const unlikeRecommendation = (item) => {
        fetchWithAuth('/api/recommendation/unlike', {
            method: 'POST',
            body: JSON.stringify({recommendationId: item.id}),
            headers: {
            'Content-Type': 'application/json'},
            credentials: 'include'})
            .then(response => {
                if(response.ok) {
                    displayActionResponse("Unliked!", ResponseTypes.SUCCESS);
                    item.likes = item.likes - 1;
                    item.likedByUser = false;
                    return;
                }
                displayActionResponse("An error occured: " + response.status, ResponseTypes.ERROR_UNKNOWN);
                return;
            });
    };
    
    const handlePlaceChange = (event) => {
        setPlace(event.target.value);
    };
    
    const handlePlaceKeyDown = (event) => {
        if (event.key === 'Enter') {
            createRecommendationStream();
        }
    };
    
    const handleChildFriendlyChange = (event) => {
        setChildFriendly(event.target.checked);
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
    
    const createLink = (item) => {
        const searchQuery = (place + " " + item).replace(/\s+/g, '+');
        return "https://www.google.com/maps/search/" + searchQuery; 
    };
    
    useEffect(() => {  
        if (hasGuideLink()) {
            try {
                resolveGuideLink();
                setReadyForSearch(true);
            }
            catch(error) {
                logError(error);
                displayActionResponse("The provided guide link was invalid!", ResponseTypes.ERROR_UNKNOWN);
            }
        }
     }, [ResponseTypes.ERROR_UNKNOWN, hasGuideLink, resolveGuideLink]);
     
    useEffect(() => {
        if (readyForSearch) {
            createRecommendationStream();
        }
     }, [readyForSearch, createRecommendationStream]);
     
    useEffect(() => {
        return () => {
            if (!state.travelGuide) {
                return;
            }
            navigate('.', { replace: true, state: state });
            setChildFriendly(state.childFriendly);
            //setTravelGuide(state.travelGuide);
            setPlace(state.place);
        };
    }, [state, navigate]);

    return (
        <div className="content"> 
            <UserInfo />
            <p>Select a country, region, or city to receive travel recommendations.</p>
            <div className="content-group">
                <input className={responseType === ResponseTypes.ERROR_PLACE ? "error-highlight" : ""} 
                       type="text" placeholder="Place" id="place" value={place} onChange={handlePlaceChange}
                       onKeyDown={handlePlaceKeyDown} />
                <span>
                    <input type="checkbox" checked={childFriendly} onChange={handleChildFriendlyChange} />
                    Search for child-friendly places
                </span>
                <button type="button" disabled={loading} onClick={createRecommendationStream}><span className={loading ? "loading-button" : ""}></span>Create Travel Guide</button>
            </div>
            
            <ul className="grid-list">
                {recommendations && recommendations.map(item => (
                <Link to={createLink(item.name)} target="_blank" onClick={(e) => {if (!item.loaded) { e.preventDefault();}}} className={`simple-item ${item.loaded ? "loaded" : "teaser"}`}  key={item.id}>
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem'}}>
                      <div>
                        <strong>{item.name}</strong> - {item.description}
                      </div>
                      <div className="recommendationLikeBox" onClick={(e) => e.preventDefault()}>
                        <span>{item.likes}</span>
                        {!item.likedByUser && (
                        <button onClick={() => likeRecommendation(item)} className="likeButton">
                            <FaRegHeart size={20} color="#999" />
                        </button>
                        )}
                        {item.likedByUser && (
                        <button onClick={() => unlikeRecommendation(item)} className="likeButton">
                            <FaHeart size={20} color="#e74c3c" />
                        </button>
                        )}
                      </div>
                    </div>
                </Link>
                ))}
            </ul>

            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
            
            <div className="footer">
                {recommendations.length > 0 && (
                        <button type="button" className="footer-button" hidden={!recommendations} onClick={copyGuideLink}>Copy Link To Guide</button>
                )}
                <Footer/>
            </div>                      
        </div>
    );
};

export default TravelGuide;
