import './App.css';
import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from 'react-router-dom';

const Story = () => {
    const navigate = useNavigate();
    const [isRunning, setIsRunning] = useState(false);    
    const apiUrl = process.env.REACT_APP_API_URL;
    const [story, setStory] = useState(null);    
    const [currentPart, setCurrentPart] = useState(0);
    
    const music = useRef(null);
    const voice = useRef(null);
    const timeout = useRef(null);
    let partsLoaded = 0;
    let part = 0;
    let voiceLib = [];
    const wakeLock = useRef(null);
    
    useEffect(() => {  
        fetch(`${apiUrl}/api/story/retrieve`, {
            method: 'GET'
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return [];
            }
        })
        .then(data => {
            setStory(data);
        })      
        .catch(error => {

        });
        
        return () => {
            stopStory();
        };
        
     }, []);
     
     const fadeOutMusic = () => {
        music.current.volume = 0.75;
        setTimeout(() => {
            music.current.volume = 0.5;
        }, 1000);
        setTimeout(() => {
            music.current.volume = 0.25;
        }, 2000);
        setTimeout(() => {
            music.current.pause();
        }, 3000);
     };


    const preloadPlayback = () => { 
        setIsRunning(true);
        
        music.current = new Audio(story.music);
        music.current.play();

        // avoid iPhone audio playback restrictions
        for (let i=0; i < story.parts.length; ++i) {
            voiceLib[i] = new Audio(story.parts[i].audio);

            voiceLib[i].addEventListener('loadedmetadata', () => {
                updateLoadingStatus();
            });
            voiceLib[i].load();
        }
    };
    
    const updateLoadingStatus = () => {
        partsLoaded = partsLoaded + 1;
        if (partsLoaded !== story.parts.length) {
            return;
        }
        acquireWakeLock();
        continuePlayback();
    };
    
    const continuePlayback = () => {
        if (part >= story.parts.length) {
            fadeOutMusic();
            releaseWakeLock();
            return;
        }
        
        voice.current = voiceLib[part];
        
        timeout.current= setTimeout(() => {
            continuePlayback();
        }, (1 + voice.current.duration) * 1000);

        setCurrentPart(prevPart => prevPart + 1);
        part = part + 1;

        voice.current.play();
        scrollToEnd();
    };

    
    const scrollToEnd = () => {
        window.scrollTo({
            top: document.documentElement.scrollHeight,
            behavior: 'smooth'
        });
    };
    
    const stopStory = () => {  
        setIsRunning(false);
        if (music.current) {
            music.current.pause();
            music.current = null;
        }
        if (voice.current) {
            voice.current.pause();
            voice.current = null;
        }
        
        if (timeout.current) {
            clearTimeout(timeout.current);
        }
        
        setCurrentPart(0);
        part = 0;
        partsLoaded=0;
        releaseWakeLock();
    };
    
    const acquireWakeLock = async () => {
        wakeLock.current = await navigator.wakeLock.request('screen');
    };
    
    const releaseWakeLock = () => {
        if (wakeLock && wakeLock.current) {
            wakeLock.current.release();   
        }
    };
     
    return (
        <div className="content">
            <div className="user-info">
                <h2>Credible Stories</h2>
                <button type="button" className="red-button" onClick={() => navigate('/')}>Login</button>
            </div>
            
            <div className="content-group">
                { isRunning ?
                <button onClick={stopStory}>Stop</button>
                :
                <button disabled={!story} onClick={preloadPlayback}>{!story ? 'Loading...' : 'Start Adventure'}</button>
                }
            </div>
            
            { story &&
            <div className="story">
                <h2 className='fade-in visible'>{story.title}</h2>
                {story.parts.map((item, index) => (
                    index <= currentPart &&
                    <div className={`fade-in ${index < currentPart ? 'visible' : ''}`} key={index}>
                        <img className='story-image' src={item.image}/>
                        <p>{item.text}</p>    
                    </div>
                ))}
            </div>
            }
        </div>
    );
};

export default Story;
