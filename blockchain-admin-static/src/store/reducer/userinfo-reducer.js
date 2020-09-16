import { UserActions } from '../action'

export default function UserinfoReducer(state = null, action) {
    switch (action.type) {
        case UserActions.SET_USERINFO:
            return { ...state, ...action.text }
        default:
            return state
    }
}