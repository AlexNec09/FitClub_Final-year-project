const initialState = {
    id: 0,
    username: '',
    displayName: '',
    image: '',
    password: '',
    isLoggedIn: false,
    isTokenValid: false,
};

export default function authReducer(state = initialState, action) {
    if (action.type === 'logout-success') {
        return { ...initialState };
    } else if (action.type === 'login-success') {
        return {
            ...action.payload,
            isLoggedIn: true,
            isTokenValid: true
        };
    } else if (action.type === 'update-success') {
        return {
            ...state,
            displayName: action.payload.displayName,
            image: action.payload.image
        };
    }
    else if (action.type === 'token-has-expired') {
        return {
            ...state,
            isTokenValid: action.payload,
        };
    }
    else if (action.type === 'confirmation-token') {
        return {
            ...state,
            emailVerificationStatus: true
        }
    }
    return state;
}