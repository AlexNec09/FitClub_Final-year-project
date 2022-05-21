import React from "react";
import { findByText, render, waitFor, fireEvent } from '@testing-library/react'
import UserList from './UserList';
import { Provider } from "react-redux";
import authReducer from "../redux/authReducer";
import * as apiCalls from "../api/apiCalls";
import { BrowserRouter } from "react-router-dom";
import { createStore } from "redux";



apiCalls.listUsers = jest.fn().mockResolvedValue({
    data: {
        content: [],
        number: 0,
        size: 10
    }
});

const defaultState = {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "image1.png",
    password: "P4ssword",
    isLoggedIn: true,
    jwt: "test-jwt-token"
};

let store;

const setup = (state = defaultState) => {
    store = createStore(authReducer, state);
    return render(<Provider store={store}>
        <BrowserRouter>
            <UserList />
        </BrowserRouter>
    </Provider>);
};

const mockedEmptySuccessResponse = {
    data: {
        content: [],
        number: 0,
        size: 10
    }
}

let userList = [
    {
        username: 'user1',
        displayName: 'display1',
        image: ''
    },
    {
        username: 'user2',
        displayName: 'display2',
        image: ''
    },
    {
        username: 'user3',
        displayName: 'display3',
        image: ''
    },
    {
        username: 'user4',
        displayName: 'display4',
        image: ''
    },
    {
        username: 'user5',
        displayName: 'display5',
        image: ''
    },
    {
        username: 'user6',
        displayName: 'displa6',
        image: ''
    },
    {
        username: 'user7',
        displayName: 'display7',
        image: ''
    },
    {
        username: 'user8',
        displayName: 'display8',
        image: ''
    },
    {
        username: 'user9',
        displayName: 'display9',
        image: ''
    },
    {
        username: 'user10',
        displayName: 'display10',
        image: ''
    },
]

const mockSuccessGetSinglePage = {
    data: {
        content: userList,
        number: 0,
        first: true,
        last: true,
        size: 10,
        totalPages: 1
    }
};

const mockSuccessGetMultiPageFirst = {
    data: {
        content: userList,
        number: 0,
        first: true,
        last: false,
        size: 10,
        totalPages: 2
    }
};

const mockSuccessGetMultiPageLast = {
    data: {
        content: [
            {
                username: 'user11',
                displayName: 'display11',
                image: ''
            }
        ],
        number: 1,
        first: false,
        last: true,
        size: 10,
        totalPages: 2
    }
};

const mockFailGet = {
    response: {
        data:
        {
            message: 'User load failed'
        }
    }
};

describe('UserList', () => {
    describe('Layout', () => {
        it('displays three items when listUser api return three users', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetSinglePage);
            const { queryByTestId } = setup();
            await waitFor(() => queryByTestId('usergroup'));
            const userGroup = queryByTestId('usergroup');
            expect(userGroup.childElementCount).toBe(10);
        });

        it('displays the displayName when listUser api returns users', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetSinglePage);
            const { queryByText } = setup();
            expect(await waitFor(() => { expect(queryByText('display1')).toBeInTheDocument() }));
        });

        it('displays the next button when response has last value as false', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetMultiPageFirst);
            const { queryByText } = setup();
            expect(await waitFor(() => { expect(queryByText('next >')).toBeInTheDocument() }));
        });

        it('hides the next button when response has last value as true', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetMultiPageLast);
            const { queryByText } = setup();
            expect(await waitFor(() => { expect(queryByText('next >')).not.toBeInTheDocument() }));
        });

        it('displays the previous button when response has first value as false', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetMultiPageLast);
            const { queryByText } = setup();
            expect(await waitFor(() => { expect(queryByText('< previous')).toBeInTheDocument() }));
        });

        it('has link to UserPage', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetMultiPageFirst);
            const { queryByText, container } = setup();
            await waitFor(() => queryByText('display1'));
            const firstAnchor = container.querySelectorAll('a')[0];
            expect(firstAnchor.getAttribute('href')).toBe('/user1');
        });

        it('displays the displayName when listUser api returns users', async () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockSuccessGetSinglePage);
            const { queryByText } = setup();
            expect(await waitFor(() => { expect(queryByText('display1')).toBeInTheDocument() }));
        });
    });

    describe('Lifecycle', () => {
        it('calls listUsers api when it is rendered', () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockedEmptySuccessResponse);
            setup();
            expect(apiCalls.listUsers).toHaveBeenCalledTimes(1);
        });

        it('calls listUsers method with page zero and size three', () => {
            apiCalls.listUsers = jest.fn().mockResolvedValue(mockedEmptySuccessResponse);
            setup();
            expect(apiCalls.listUsers).toHaveBeenCalledWith({ page: 0, size: 10 });
        });
    });

    describe('Interactions', () => {
        it('loads next page when clicked to next button', async () => {
            apiCalls.listUsers = jest.fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst)
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast);
            const { queryByText } = setup();
            const nextLink = await waitFor(() => queryByText('next >'));
            fireEvent.click(nextLink);
            expect(await waitFor(() => { expect(queryByText('display4')).toBeInTheDocument() }));
        });

        it('loads previous page when clicked to previous button', async () => {
            apiCalls.listUsers = jest.fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst);
            const { queryByText } = setup();
            const previousLink = await waitFor(() => queryByText('< previous'));
            fireEvent.click(previousLink);
            expect(await waitFor(() => { expect(queryByText('display1')).toBeInTheDocument() }));
        });

        it('displays error message when loading other page fails', async () => {
            apiCalls.listUsers = jest.fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockRejectedValueOnce(mockFailGet);
            const { queryByText } = setup();
            const previousLink = await waitFor(() => queryByText('< previous'));
            fireEvent.click(previousLink);
            expect(await waitFor(() => { expect(queryByText('User load failed!')).toBeInTheDocument() }));
        });

        it('hides error message when successfully loading other page', async () => {
            apiCalls.listUsers = jest.fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockRejectedValueOnce(mockFailGet)
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst);
            const { queryByText } = setup();
            const previousLink = await waitFor(() => queryByText('< previous'));
            fireEvent.click(previousLink);
            await waitFor(() => queryByText('User load failed'));
            fireEvent.click(previousLink);
            expect(await waitFor(() => { expect(queryByText('User load failed')).not.toBeInTheDocument() }));
        });
    });
});
