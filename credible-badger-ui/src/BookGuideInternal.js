import React, { useContext } from "react";
import UserInfo from './UserInfo';
import BookGuide from './BookGuide';
import { UserContext } from './UserContext';

const BookGuideInternal = () => {
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
                <BookGuide />
            )}
        </div>
        );
};

export default BookGuideInternal;
