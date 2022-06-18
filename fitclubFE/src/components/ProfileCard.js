import React from 'react';
import ProfileImageWithDefault from './ProfileImageWithDefault';

const ProfileCard = (props) => {
    const { displayName, username, image } = props.user;

    return (
        <div className='card'>
            <div className='card-header text-center'>
                <ProfileImageWithDefault alt="profile"
                    width="200"
                    height="200"
                    image={image}
                    src={props.loadedImage}
                    className='shadow rounded-circle' />
            </div>
            <div className='card-body text-center'>
                <div>
                    <h5 className="text-unauthorized-access-user">
                        {username}
                    </h5>
                    <h4 data-testid="displayNameTestId">{displayName}</h4>
                </div>
            </div>
        </div>
    );
};

ProfileCard.defaultProps = {
    errors: {}
}

export default ProfileCard;