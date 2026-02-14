import './App.css';
import React, { useContext, useState, useEffect } from "react";
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import { UserContext } from './UserContext';
import UserInfo from './UserInfo';
import Footer from './Footer';
import { fetchWithAuth } from './Api';
import { FaHeart, FaRegHeart } from 'react-icons/fa';

const MovieGuide = () => {
    const { guideLink } = useParams();
    const [readyForSearch, setReadyForSearch] = useState(false);
    const [actionResponse, setActionResponse] = useState([]);
    const [responseType, setResponseType] = useState([]);
    const [name, setName] = useState("");
    const [loading, setLoading] = useState(false);
    const [recommendations, setRecommendations] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const navigate = useNavigate();
    const location = useLocation();
    const [state, setState] = useState(() => location.state || {});
    const [showNotification, setShowNotification] = useState(false);
    const isMobile = /Android|iPhone|iPad|iPod|Opera Mini|IEMobile|WPDesktop/i.test(navigator.userAgent);
    const controller = new AbortController();
    const { user } = useContext(UserContext);

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
    
    async function fetchStream() {
        if (!validateInput()) {
            return;
        }  
        displayActionResponse("", ResponseTypes.SUCCESS);
        setLoading(true);
        
        const tr = Array.from({ length: 20 }, (_, i) => ({
            id: i + 1,
            name: "Recommendation Title",
            description: "Description Description Description Description Description Description"}));
        
        const response = await fetchWithAuth('/api/recommendation/movie', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"},
            body: JSON.stringify({name: name}),
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
                    console.error(err);
                    displayActionResponse("Guide creation failed: " + response.status, ResponseTypes.ERROR_UNKNOWN);
                }
            }

            readChunk(); // recursively read next chunk
        }

        readChunk();
    }
    
    const createRecommendationStream = () => {
        fetchStream().catch(err => console.error(err));
    };
    
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
    
    const handleNameChange = (event) => {
        setName(event.target.value);
    };
    
    const handleNameKeyDown = (event) => {
        if (event.key === 'Enter') {
            createRecommendationStream();
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
            createRecommendationStream();   
        }
     }, [readyForSearch]);
     
    useEffect(() => {
        return () => {
            if (!state.movieGuide) {
                return;
            }
            navigate('.', { replace: true, state: state });
            //setMovieGuide(state.movieGuide);
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
                <button type="button" disabled={loading} onClick={createRecommendationStream}><span className={loading ? "loading-button" : ""}></span>Create Movie Guide</button>
            </div>
            
            <ul className="grid-list">
                {recommendations && recommendations.map(item => (
                <Link to={createLink(item.name)} target="_blank" onClick={(e) => {if (!item.loaded) { e.preventDefault();}}} className={item.loaded ? "loaded" : "teaser"}>
                    <li key={item.id}>
                     <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem'}}>
                       <div>
                         <strong>{item.name}</strong> - {item.description}
                       </div>
                       <div className="recommendationLikeBox" onClick={(e) => e.preventDefault()}>
                         <span>{item.likes}</span>
                         {!item.likedByUser && (
                         <button onClick={(e) => likeRecommendation(item)} className="likeButton">
                             <FaRegHeart size={20} color="#999" />
                         </button>
                         )}
                         {item.likedByUser && (
                         <button onClick={(e) => unlikeRecommendation(item)} className="likeButton">
                             <FaHeart size={20} color="#e74c3c" />
                         </button>
                         )}
                       </div>
                     </div>
                   </li>
                </Link>
                ))}
            </ul>

            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
            
            <div className="footer">
                {recommendations && (
                        <button type="button" className="footer-button" hidden={!recommendations} onClick={copyGuideLink}>Copy Link To Guide</button>
                )}
                <Footer/>
            </div>           
        </div>
    );
};

export default MovieGuide;
