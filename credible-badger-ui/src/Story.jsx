import './App.css';
import React, { useCallback, useState, useEffect, useRef } from "react";
import UserInfo from './UserInfo';
import { fetchWithAuth } from './Api';
import { logError } from './Logging';

const Story = () => {
    const [isRunning, setIsRunning] = useState(false);    
    const [story, setStory] = useState(null);    
    const [currentPart, setCurrentPart] = useState(0);
    const [partsLoaded, setPartsLoaded] = useState(0);
    const [voiceLib, setVoiceLib] = useState([]);
    
    const music = useRef(null);
    const voice = useRef(null);
    const timeout = useRef(null);
    const wakeLock = useRef(null);

    const releaseWakeLock = () => {
        if (wakeLock && wakeLock.current) {
            wakeLock.current.release();   
        }
    };
    
        
    const acquireWakeLock = async () => {
        wakeLock.current = await navigator.wakeLock.request('screen');
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
        setPartsLoaded(0);
        releaseWakeLock();
    };
    
    useEffect(() => {  
        fetchWithAuth("/api/story/retrieve", {
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
            logError(error);
        });
        
        return () => {
            //stopStory();
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
        const newVoiceLib = story.parts.map((part) => {
          const audio = new Audio(part.audio);
          audio.addEventListener('loadedmetadata', updateLoadingStatus);
          audio.load();
          return audio;
        });
        setVoiceLib(newVoiceLib);
    };
    
    const updateLoadingStatus = () => {
        setPartsLoaded(prevPart => prevPart + 1);
    };
    
    const scrollToEnd = () => {
        window.scrollTo({
            top: document.documentElement.scrollHeight,
            behavior: 'smooth'
        });
    };
    
    const continuePlayback = useCallback(() => {
        if (currentPart >= story.parts.length) {
            fadeOutMusic();
            releaseWakeLock();
            return;
        }
        
        voice.current = voiceLib[currentPart];
        
        timeout.current= setTimeout(() => {
            setCurrentPart(prevPart => prevPart + 1);
            scrollToEnd();
        }, (1 + voice.current.duration) * 1000);


        voice.current.play();
    }, [currentPart, story, voiceLib]);
    
    useEffect(() => {
        if (story && partsLoaded === story.parts.length) {
          acquireWakeLock();
          continuePlayback();
        }
     }, [continuePlayback, story, partsLoaded, currentPart]);

    return (
        <div className="content">
            <UserInfo />    
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
                    index <= currentPart + 1 &&
                    <div className={`fade-in ${isRunning && index <= currentPart ? 'visible' : ''}`} key={index}>
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
