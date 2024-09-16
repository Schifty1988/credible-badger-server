import React, { useContext, useState, useEffect } from "react";
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';

const Feedback = () => {
    const { user } = useContext(UserContext);
    const [feedback, setFeedback] = useState([]);
    const apiUrl = process.env.REACT_APP_API_URL;
    const [needHelp, setNeedHelp] = useState(false);
    
    console.log(user);
    
    const tutorialData = {
        projectKey : user ? user.id : "Loading...",
        projectVersion : "0.0.9-BETA",
        projectUser : "unknownUser",
        content : "I like the new patch!"
    };
    
    
    useEffect(() => {
        fetch(`${apiUrl}/api/feedback/retrieve`, {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
        })
        .then(response => {
                if (!response.ok) {
                    return [];
                }
                return response.json();
        })
        .then(data => setFeedback(data))
        .catch(error => {
            setFeedback([]);
        });
    }, []);
    
    const toggleNeedHelp = () => {
        setNeedHelp(!needHelp);
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
                <p>
                    Send over your customer feedback with <a onClick={toggleNeedHelp}>a single REST call.</a> Manage it right here.
                </p>
                {needHelp &&
                <div>
                    <p>Send a POST request to https://crediblebadger.com/api/feedback/submit</p>
                    <pre className="json-code">{JSON.stringify(tutorialData, null, 2)}</pre>
                </div>
                }

                { feedback.length === 0 && <p>No feedback was submitted yet!</p> }
                
                <ul className="simple-list">
                {feedback.map(item => (
                    <li key={item.id} className="feedback-item">
                        {item.content}
                        <div className="feedback-footer">
                            {item.projectUser} | {item.projectVersion} | {item.creationTime.slice(0,16).replace('T', '-')}
                        </div>
                    </li>
                ))}
                </ul>
            </div>    
            )}                                    
            <div className="footer">
                <Footer/>
            </div>
        </div>
        );
};

export default Feedback;
