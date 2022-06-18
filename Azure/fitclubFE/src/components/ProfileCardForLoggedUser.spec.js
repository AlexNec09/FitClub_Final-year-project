import React from 'react';
import { render } from '@testing-library/react';
import { Provider } from "react-redux";
import { createStore } from "redux";
import authReducer from "../redux/authReducer";
import { MemoryRouter } from "react-router-dom";
import { IMAGES_PROFILE } from "../config";
import ProfileCardForLoggedUser from './ProfileCardForLoggedUser';

const defaultUserState = {
    id: 1,
    username: 'user1',
    displayName: 'display1',
    image: 'profile.png',
    isLoggedIn: true,
};

const userStateWithoutImage = {
    id: 1,
    username: 'user1',
    displayName: 'display1',
    isLoggedIn: true,
    image: undefined
}

const setup = (props, state = defaultUserState) => {
    const store = createStore(authReducer, state);
    return render(
        <Provider store={store}>
            <MemoryRouter>
                <ProfileCardForLoggedUser user={defaultUserState} {...props} />
            </MemoryRouter>
        </Provider>
    );
};

describe('ProfileCard', () => {
    describe('Layout', () => {
        it('displays he displayName@username', () => {
            const { queryByText } = setup();
            const userInfo = queryByText('display1');
            expect(userInfo).toBeInTheDocument();
        });

        it('has image', () => {
            const { container } = setup();
            const image = container.querySelector('img');
            expect(image).toBeInTheDocument();
        });

        it('displays default image when user does not have one', () => {
            const { container } = setup(userStateWithoutImage);
            const image = container.querySelector('img');
            expect(image.src).toContain(IMAGES_PROFILE + '/profile.png');
        });

        it('displays user image if user has one', () => {
            const { container } = setup();
            const image = container.querySelector('img');
            expect(image.src).toContain('/images/profile/' + defaultUserState.image);
        });

        it('displays edit button when isEditable property set as true', () => {
            const { queryByText } = setup({ isEditable: true });
            const editButton = queryByText('Edit Profile');
            expect(editButton).toBeInTheDocument();
        });

        it('does not display edit button when isEditable not provided', () => {
            const { queryByText } = setup();
            const editButton = queryByText('Edit Profile');
            expect(editButton).not.toBeInTheDocument();
        });

        it('displays displayName input when inEditMode property set as true', () => {
            const { container } = setup({ inEditMode: true });
            const displayInput = container.querySelector('input');
            expect(displayInput).toBeInTheDocument();
        });

        it('displays the current displayName in input in edit mode', () => {
            const { container } = setup({ inEditMode: true });
            const displayInput = container.querySelector('input');
            expect(displayInput.value).toBe(defaultUserState.displayName);
        });

        it('hides the displayName@username in edit mode', () => {
            const { queryByText } = setup({ inEditMode: true });
            const userInfo = queryByText('display1');
            expect(userInfo).not.toBeInTheDocument();
        });

        it('displays input for displayName in edit mode', () => {
            const { container } = setup({ inEditMode: true }, defaultUserState);
            const input = container.querySelector('Input');
            expect(input).toBeInTheDocument();
        });

        it('hides the edit button in edit mode and isEditable provided as true', () => {
            const { queryByText } = setup({ inEditMode: true, isEditable: true });
            const editButton = queryByText('Edit Profile');
            expect(editButton).not.toBeInTheDocument();
        });

        it('displays Save button in edit mode', () => {
            const { queryByText } = setup({ inEditMode: true, isEditable: true });
            const saveButton = queryByText('Save');
            expect(saveButton).toBeInTheDocument();
        });

        it('displays Cancel button in edit mode', () => {
            const { queryByText } = setup({ inEditMode: true, isEditable: true });
            const cancelButton = queryByText('Cancel');
            expect(cancelButton).toBeInTheDocument();
        });

        it('displays file input when inEditMode property set as true', () => {
            const { container } = setup({ inEditMode: true });
            const inputs = container.querySelectorAll('input');
            const uploadInput = inputs[1];
            expect(uploadInput.type).toBe('file');

        });
    });
});