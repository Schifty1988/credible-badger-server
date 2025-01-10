import './App.css';
import React, {useState, useContext} from "react";
import { useNavigate } from 'react-router-dom';
import { UserContext } from './UserContext';

const Footer = () => {
    const navigate = useNavigate();
    const [showFeedbackSubmission, setShowFeedbackSubmission] = useState(false);
    const [feedback, setFeedback] = useState("");
    const [submitted, setSubmitted] = useState(false);
    const [submissionFailed, setSubmissionFailed] = useState(false);
    const apiUrl = process.env.REACT_APP_API_URL;
    const projectKey = process.env.REACT_APP_FEEDBACK_PROJECT_KEY;
    const projectVersion = process.env.REACT_APP_FEEDBACK_PROJECT_VERSION;
    const { user } = useContext(UserContext);
    
    const submitFeedback = () => {
        const userName = user ? user.email : "Anonymous";
        
        if (feedback.length === 0) {
            return;
        }
        
        fetch(`${apiUrl}/api/feedback/submit`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({projectKey: projectKey, projectVersion: projectVersion, projectUser: userName, content: feedback})
        })
        .then(response => {
            if (response.ok) {
                setFeedback("");
                setSubmitted(true);
            }
            return [];
        });
    };
    
    const initFeedbackForm = () => {
        setSubmitted(false);
        setShowFeedbackSubmission(true);
    };
    
    const handleFeedbackChange = (event) => {
        setFeedback(event.target.value);
    };

    return (
        <React.Fragment>
            <button className ="footer-button" onClick={() => initFeedbackForm()}>Give Us Feedback</button>
            <button className = "footer-button" onClick={() => navigate("/legal")}>Terms of Service & Privacy Policy</button>
            
            { showFeedbackSubmission &&
                <div className="feedback-submission">
                    <div className="feedback-submission-overlay" onClick={() => setShowFeedbackSubmission(false)}/>
                    <div className="feedback-submission-content">
                        { submitted ? 
                            <h2 className="feedback-submission-thank-you">Thank You!</h2>                            
                        :
                        (
                        <div>
                            <h2 className="feedback-submission-title">What's on your mind?</h2>
                            <textarea value={feedback} placeholder="Share your thoughts..." onChange={handleFeedbackChange}/>
                            <button disabled={feedback.length === 0} onClick={submitFeedback}>Submit</button>
                            <button className="button-margin-left" onClick={() => setShowFeedbackSubmission(false)}>Close</button>
                        </div>
                        )}
                    </div>
                </div>
            }
        </React.Fragment>
    );
};

export default Footer;
