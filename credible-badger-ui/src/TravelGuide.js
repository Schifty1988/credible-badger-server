import './App.css';
import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import UserInfo from './UserInfo';
import Footer from './Footer';

const TravelGuide = () => {
    const { guideLink } = useParams();
    const [readyForSearch, setReadyForSearch] = useState(false);
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const [place, setPlace] = useState("");
    const [loading, setLoading] = useState(false);
    const [childFriendly, setChildFriendly] = useState("false");
    const [travelGuide, setTravelGuide] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    const location = useLocation();
    const [state, setState] = useState(() => location.state || {});
    const [showNotification, setShowNotification] = useState(false);
    const isMobile = /Android|iPhone|iPad|iPod|Opera Mini|IEMobile|WPDesktop/i.test(navigator.userAgent);

    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown',
        ERROR_PLACE: 'error_place'
    };
    
    const hasGuideLink = () => {
        return guideLink && guideLink.length > 0;
    };
    
    const resolveGuideLink = () => {
        const jsonGuideLink = atob(guideLink);
        const decodedJson = JSON.parse(jsonGuideLink);
        setPlace(decodedJson.place);
        setChildFriendly(decodedJson.childFriendly);
    };
    
    const copyGuideLink = () => {
        const guideRequest = JSON.stringify({place: place, childFriendly: childFriendly});   
        const encodedGuideRequest = btoa(guideRequest);
        const link  = apiUrl + "/travelGuide/" + encodedGuideRequest;
        navigator.clipboard.writeText(link);
        
        if (!isMobile) {
            displayActionResponse("Link was copied!", ResponseTypes.SUCCESS);   
        }
    };
    
    const validateInput = () => {
        if (!clientValidationPlace()) {
            displayActionResponse("The provided place seems invalid.", ResponseTypes.ERROR_PLACE);
            return false;
        }   
        return true;
    };    

    const clientValidationPlace = () => {
        return place.length > 1;
    };
    
    const createTravelGuide = () => {      
        if (!validateInput()) {
            return;
        }
        displayActionResponse("", ResponseTypes.SUCCESS);
        setLoading(true);
        
        fetch(`${apiUrl}/api/travel/travelGuide`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({place: place, childFriendly: childFriendly})
        })
        .then(response => {
            if (response.ok) {
                displayActionResponse("Guide was created!", ResponseTypes.SUCCESS);
                return response.json();
            } else {
                displayActionResponse("Guide creation failed: " + response.status, ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })
        .then(data => {
            const stateobj = {travelGuide: data, childFriendly : childFriendly, place : place};
            navigate('.', { replace: true, state: stateobj});
            setTravelGuide(data);
            setLoading(false);
        })      
        .catch(error => {
            displayActionResponse("Guide creation failed: " + error.message, ResponseTypes.ERROR_UNKNOWN);
            setLoading(false);
        });
    };    
    
    const handlePlaceChange = (event) => {
        setPlace(event.target.value);
    };
    
    const handlePlaceKeyDown = (event) => {
        if (event.key === 'Enter') {
            createTravelGuide();
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
                displayActionResponse("The provided guide link was invalid!", ResponseTypes.ERROR_UNKNOWN);
            }
        }
     }, []);
     
    useEffect(() => {
        if (readyForSearch) {
            createTravelGuide();   
        }
     }, [readyForSearch]);
     
    useEffect(() => {
        return () => {
            if (!state.travelGuide) {
                return;
            }
            navigate('.', { replace: true, state: state });
            setChildFriendly(state.childFriendly);
            setTravelGuide(state.travelGuide);
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
                <button type="button" disabled={loading} onClick={createTravelGuide}><span className={loading ? "loading-button" : ""}></span>Create Travel Guide</button>
            </div>
            
            <ul className="grid-list">
                {travelGuide.travelRecommendations && travelGuide.travelRecommendations.map(item => (
                    <Link key={item.id} to={createLink(item.name)} className="travel-guide-link">
                        <li className="point-of-interest"><b>{item.name}</b> - {item.description}</li>
                    </Link>
                ))}
            </ul>

            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
            
            <div className="footer">
                {travelGuide.travelRecommendations && (
                        <button type="button" className="footer-button" hidden={!travelGuide.travelRecommendations} onClick={copyGuideLink}>Copy Link To Guide</button>
                )}
                <Footer/>
            </div>                      
        </div>
    );
};

export default TravelGuide;
