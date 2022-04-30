import React from 'react';
import { render, fireEvent, waitFor, waitForElementToBeRemoved } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import { LoginPage } from './LoginPage';

//beforeEach(cleanup);

describe('LoginPage', () => {
    describe('Layout', () => {
        it('has header of Login', () => {
            const { container } = render(<LoginPage />);
            const header = container.querySelector('h1');
            expect(header).toHaveTextContent('Login');
        });

        it('has input for username', () => {
            const { queryByPlaceholderText } = render(<LoginPage />);
            const usernameInput = queryByPlaceholderText('Your username')
            expect(usernameInput).toBeInTheDocument();
        });

        it('has input for password', () => {
            const { queryByPlaceholderText } = render(<LoginPage />);
            const paswordInput = queryByPlaceholderText('Your password')
            expect(paswordInput).toBeInTheDocument();
        });

        it('has password type for password input', () => {
            const { queryByPlaceholderText } = render(<LoginPage />);
            const paswordInput = queryByPlaceholderText('Your password')
            expect(paswordInput.type).toBe('password');
        });

        it('has login button', () => {
            const { container } = render(<LoginPage />);
            const button = container.querySelector('button');
            expect(button).toBeInTheDocument();
        });
    });
    describe('Interactions', () => {
        const changeEvent = (content) => {
            return {
                target: {
                    value: content
                }
            };
        };

        const mockAsyncDelayed = () => {
            return jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({})
                    }, 300)
                })
            });
        };

        let usernameInput, passwordInput, button;

        const setupForSubmit = (props) => {
            const rendered = render(<LoginPage {...props} />);
            const { container, queryByPlaceholderText } = rendered;

            usernameInput = queryByPlaceholderText('Your username');
            fireEvent.change(usernameInput, changeEvent('my-user-name'));
            passwordInput = queryByPlaceholderText('Your password');
            fireEvent.change(passwordInput, changeEvent('P4ssword'));
            button = container.querySelector('button');

            return rendered;
        }

        it('sets the username value into state', () => {
            const { queryByPlaceholderText } = render(<LoginPage />);
            const usernameInput = queryByPlaceholderText('Your username');
            fireEvent.change(usernameInput, changeEvent('my-user-name'));
            expect(usernameInput).toHaveValue('my-user-name');
        });

        it('sets the password value into state', () => {
            const { queryByPlaceholderText } = render(<LoginPage />);
            const passwordInput = queryByPlaceholderText('Your password');
            fireEvent.change(passwordInput, changeEvent('P4ssword'));
            expect(passwordInput).toHaveValue('P4ssword');
        });

        it('calls postLogin when the actions are provided in props and input fields have value', () => {
            const actions = {
                postLogin: jest.fn().mockResolvedValue({})
            }
            setupForSubmit({ actions });

            fireEvent.click(button);
            expect(actions.postLogin).toHaveBeenCalledTimes(1);
        });

        it('does not throw exception when clicking the button when actions not provided in props', () => {
            setupForSubmit();
            expect(() => fireEvent.click(button)).not.toThrow();
        });

        it('calls postLogin with credentials in body', () => {
            const actions = {
                postLogin: jest.fn().mockResolvedValue({})
            }
            setupForSubmit({ actions });
            fireEvent.click(button);

            const expectedUserObject = {
                username: 'my-user-name',
                password: 'P4ssword'
            }
            expect(actions.postLogin).toHaveBeenCalledWith(expectedUserObject);
        });

        it('enables the button when username and password is not empty', () => {
            setupForSubmit();
            expect(button).not.toBeDisabled();
        });

        it('disables the button when username is empty', () => {
            setupForSubmit();
            fireEvent.change(usernameInput, changeEvent(''));
            expect(button).toBeDisabled();
        });

        it('disables the button when password is empty', () => {
            setupForSubmit();
            fireEvent.change(passwordInput, changeEvent(''));
            expect(button).toBeDisabled();
        });

        it('displays alert when login fails', async () => {
            const actions = {
                postLogin: jest.fn().mockRejectedValue({
                    response: {
                        data: {
                            message: 'Login failed!'
                        }
                    }
                })
            };
            const { queryByText } = setupForSubmit({ actions });
            fireEvent.click(button);

            await waitFor(() => expect(queryByText('Login failed!')).toBeInTheDocument()); // good test using waitFor
        });

        it('clears alert when user changes username', async () => {
            const actions = {
                postLogin: jest.fn().mockRejectedValue({
                    response: {
                        data: {
                            message: 'Login failed!'
                        }
                    }
                })
            };
            const { queryByText } = setupForSubmit({ actions });
            fireEvent.click(button);

            fireEvent.change(usernameInput, changeEvent('updated-username'));
            await waitFor(() => expect(queryByText('Login failed!')).not.toBeInTheDocument());
        });

        it('clears alert when user changes password', async () => {
            const actions = {
                postLogin: jest.fn().mockRejectedValue({
                    response: {
                        data: {
                            message: 'Login failed!'
                        }
                    }
                })
            };
            const { queryByText } = setupForSubmit({ actions });
            fireEvent.click(button);

            fireEvent.change(passwordInput, changeEvent('updated-P4ssword'));
            await waitFor(() => expect(queryByText('Login failed!')).not.toBeInTheDocument());
        });

        it('does not allow user to click the Login button when there is an ongoing api call', () => {
            const actions = {
                postLogin: mockAsyncDelayed()
            };
            setupForSubmit({ actions });
            fireEvent.click(button);
            expect(actions.postLogin).toHaveBeenCalledTimes(1);
        });

        it('displays spinner when there is an ongoing api call', () => {
            const actions = {
                postLogin: mockAsyncDelayed()
            };
            const { queryByRole } = setupForSubmit({ actions });
            fireEvent.click(button);
            const spinner = queryByRole("status");
            expect(spinner).toBeInTheDocument();
        });

        it('hides spinner after api call finishes successfully', async () => {
            const actions = {
                postLogin: mockAsyncDelayed()
            };
            const { queryByRole } = setupForSubmit({ actions });
            fireEvent.click(button);

            await waitForElementToBeRemoved(() => queryByRole("status"));

            const spinner = queryByRole("status");
            expect(spinner).not.toBeInTheDocument();
        });

        it('hides spinner after api call finishes with error', async () => {
            const actions = {
                postLogin: jest.fn().mockImplementation(() => {
                    return new Promise((resolve, reject) => {
                        setTimeout(() => {
                            reject({
                                response: { data: {} }
                            })
                        }, 300)
                    })
                })
            };
            const { queryByRole } = setupForSubmit({ actions });
            fireEvent.click(button);

            await waitForElementToBeRemoved(() => queryByRole("status"));

            const spinner = queryByRole("status");
            expect(spinner).not.toBeInTheDocument();
        });

        it('redirects to homePage after successful login', async () => {
            const actions = {
                postLogin: jest.fn().mockResolvedValueOnce({})
            };
            const history = {
                push: jest.fn()
            };

            const { queryByRole } = setupForSubmit({ actions, history });
            fireEvent.click(button);
            await waitForElementToBeRemoved(() => queryByRole("status"));

            expect(history.push).toHaveBeenCalledWith('/');
        });
    });
})

console.error = () => { };