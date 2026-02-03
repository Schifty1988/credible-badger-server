import './App.css';
import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate, useLocation } from 'react-router-dom';
import UserInfo from './UserInfo';
import Footer from './Footer';
import { fetchWithAuth } from './Api';

const BookGuide = () => {
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
        const link  = apiUrl + "/bookGuide/" + encodedGuideRequest;
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
        
        const response = await fetchWithAuth('/api/recommendation/book', {
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
                    json.id = index;
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
        const searchQuery = "book+" + (item).replace(/\s+/g, '+');
        return "https://www.amazon.com/s?k=" + searchQuery; 
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
            if (!state.bookGuide) {
                return;
            }
            navigate('.', { replace: true, state: state });
            //setBookGuide(state.bookGuide);
            setName(state.name);
        };
    }, [state, navigate]);

    return (
        <div className="content"> 
            <UserInfo />
            <p>Enter the name of a book and create a list of similar books.</p>
            <div className="content-group">
                <input className={responseType === ResponseTypes.ERROR_NAME ? "error-highlight" : ""} 
                type="text" placeholder="Book Title" id="bookName" value={name} onChange={handleNameChange}
                onKeyDown={handleNameKeyDown} />
                <button type="button" disabled={loading} onClick={createRecommendationStream}><span className={loading ? "loading-button" : ""}></span>Create Book Guide</button>
            </div>
            
            <ul className="grid-list">
                {recommendations && recommendations.map(item => (
                    <Link key={item.id} to={createLink(item.name)} onClick={(e) => {if (!item.loaded) { e.preventDefault();}}} className={item.loaded ? "loaded" : "teaser"}>
                        <li className="recommendation"><b>{item.name}</b> - {item.description}</li>
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

export default BookGuide;
