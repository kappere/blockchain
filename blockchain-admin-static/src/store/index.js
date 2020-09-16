import {createStore, combineReducers} from 'redux'
import UserinfoReducer from './reducer/userinfo-reducer.js'

const reducers = combineReducers({
    UserinfoReducer
})
let store = createStore(reducers)
export default store