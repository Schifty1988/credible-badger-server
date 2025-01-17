import React, { useContext } from "react";
import UserInfo from './UserInfo';
import MovieGuide from './MovieGuide';
import { UserContext } from './UserContext';

const MovieGuideInternal = () => {
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
                <MovieGuide />
            )}
        </div>
        );
};

export default MovieGuideInternal;
