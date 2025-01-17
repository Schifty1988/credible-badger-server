import React, { useContext } from "react";
import UserInfo from './UserInfo';
import TravelGuide from './TravelGuide';
import { UserContext } from './UserContext';

const TravelGuideInternal = () => {
    const { user } = useContext(UserContext);

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
                <TravelGuide />
            )}
        </div>
        );
};

export default TravelGuideInternal;
