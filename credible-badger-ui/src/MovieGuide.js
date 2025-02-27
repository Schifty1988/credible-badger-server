import './App.css';
import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import UserInfo from './UserInfo';
import Footer from './Footer';

const MovieGuide = () => {
    const { guideLink } = useParams();
    const [readyForSearch, setReadyForSearch] = useState(false);
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const [name, setName] = useState("");
    const [loading, setLoading] = useState(false);
    const [movieGuide, setMovieGuide] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    const location = useLocation();
    const [state, setState] = useState(() => location.state || {});
    const [showNotification, setShowNotification] = useState(false);
    const isMobile = /Android|iPhone|iPad|iPod|Opera Mini|IEMobile|WPDesktop/i.test(navigator.userAgent);

    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown',
        ERROR_NAME: 'error_name'
    };
    
    const hasGuideLink = () => {
        return guideLink && guideLink.length > 0;
    };
    
    const resolveGuideLink = () => {
        const jsonGuideLink = atob(guideLink);
        const decodedJson = JSON.parse(jsonGuideLink);
        setName(decodedJson.name);
    };
    
    const copyGuideLink = () => {
        const guideRequest = JSON.stringify({name: name});   
        const encodedGuideRequest = btoa(guideRequest);
        const link  = apiUrl + "/movieGuide/" + encodedGuideRequest;
        navigator.clipboard.writeText(link);
        
        if (!isMobile) {
            displayActionResponse("Link was copied!", ResponseTypes.SUCCESS);   
        }
    };
    
    const validateInput = () => {
        if (!clientValidationName()) {
            displayActionResponse("The provided title seems invalid.", ResponseTypes.ERROR_NAME);
            return false;
        }   
        return true;
    };    

    const clientValidationName = () => {
        return name.length > 1;
    };
    
    const createMovieGuide = () => {      
        if (!validateInput()) {
            return;
        }
        displayActionResponse("", ResponseTypes.SUCCESS);
        setLoading(true);
        
        fetch(`${apiUrl}/api/movie/movieGuide`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name: name})
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
            const stateobj = {movieGuide: data, name : name};
            navigate('.', { replace: true, state: stateobj});
            setMovieGuide(data);
            setLoading(false);
        })      
        .catch(error => {
            displayActionResponse("Guide creation failed: " + error.message, ResponseTypes.ERROR_UNKNOWN);
            setLoading(false);
        });
    };    
    
    const handleNameChange = (event) => {
        setName(event.target.value);
    };
    
    const handleNameKeyDown = (event) => {
        if (event.key === 'Enter') {
            createMovieGuide();
        }
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
        const searchQuery = (item).replace(/\s+/g, '+');
        return "https://www.imdb.com/find/?q=" + searchQuery; 
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
            createMovieGuide();   
        }
     }, [readyForSearch]);
     
    useEffect(() => {
        return () => {
            if (!state.movieGuide) {
                return;
            }
            navigate('.', { replace: true, state: state });
            setMovieGuide(state.movieGuide);
            setName(state.name);
        };
    }, [state, navigate]);

    return (
        <div className="content"> 
            <UserInfo />
            <p>Enter the name of a movie and create a list of similar movies.</p>
            <div className="content-group">
                <input className={responseType === ResponseTypes.ERROR_NAME ? "error-highlight" : ""} 
                type="text" placeholder="Movie Title" id="movieName" value={name} onChange={handleNameChange}
                onKeyDown={handleNameKeyDown} />
                <button type="button" disabled={loading} onClick={createMovieGuide}><span className={loading ? "loading-button" : ""}></span>Create Movie Guide</button>
            </div>
            
            <ul className="grid-list">
                {movieGuide.movieRecommendations && movieGuide.movieRecommendations.map(item => (
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
                {movieGuide.movieRecommendations && (
                        <button type="button" className="footer-button" hidden={!movieGuide.movieRecommendations} onClick={copyGuideLink}>Copy Link To Guide</button>
                )}
                <Footer/>
            </div>           
        </div>
    );
};

export default MovieGuide;
