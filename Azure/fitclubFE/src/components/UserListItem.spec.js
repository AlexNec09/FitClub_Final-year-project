import React from 'react';
import { render } from '@testing-library/react';
import UserListItem from './UserListItem';
import { MemoryRouter } from "react-router-dom";
import { IMAGES_PROFILE } from '../config';

const user = {
    username: 'user1',
    displayName: 'display1',
    image: 'image1.png'
};

const setup = (propUser = user) => {
    return render(
        <MemoryRouter>
            <UserListItem user={propUser} />
        </MemoryRouter>
    )
}

describe('UserListItem', () => {
    it('has image', () => {
        const { container } = setup();
        const image = container.querySelector('img');
        expect(image).toBeInTheDocument();
    });

    it('displays default image when user does not have one', () => {
        const userWithoutImage = {
            ...user,
            image: undefined
        }
        const { container } = setup(userWithoutImage);
        const image = container.querySelector('img');
        expect(image.src).toContain('/profile.png');
    });

    it('displays users image when user have one', () => {
        const { container } = setup();
        const image = container.querySelector('img');
        expect(image.src).toContain(IMAGES_PROFILE + '/' + user.image);
    });
});