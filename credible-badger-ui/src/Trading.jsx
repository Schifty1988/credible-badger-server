import React, { useContext, useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from 'react-router-dom';
import UserInfo from './UserInfo';
import { UserContext } from './UserContext';
import Footer from './Footer';
import { FaRegCalendarAlt } from "react-icons/fa";
import { API_URL, fetchWithAuth } from './Api';
import { logError } from './Logging';

const Trading = () => {
    const { user } = useContext(UserContext);
    const { userId } = useParams();
    const [trades, setTrades] = useState([]);
    const [tradeSummary] = useState([]);
    const [tradeFilter, setTradeFilter] = useState('all');
    const [yearFilter, setYearFilter] = useState(new Date().getFullYear());
    const navigate = useNavigate();
    const [interactionMarker, setInteractionMarker] = useState(null);
    
    const [showNotification, setShowNotification] = useState(false);
    const [responseType, setResponseType] = useState([]);
    const [actionResponse, setActionResponse] = useState([]);
    
    const [addTradeSymbol, setAddTradeSymbol]  = useState('');
    const [addTradeQuantity, setAddTradeQuantity]  = useState('');  
    const [addTradePurchasePrice, setAddTradePurchasePrice]  = useState('');
    const [addTradeSalePrice, setAddTradeSalePrice]  = useState('');
    
    const today = new Date().toISOString();
    const todaySplit = today.split('T')[0];
    
    const [addTradePurchaseDate, setAddTradePurchaseDate]  = useState(todaySplit);
    const [addTradeSaleDate, setAddTradeSaleDate]  = useState(todaySplit);
    
    const ResponseTypes = {
        SUCCESS: 'success',
        ERROR_UNKNOWN: 'error_unknown'
    };
    
    const processTrades = useCallback((newTrades) => {  
        if (!newTrades || newTrades.length === 0) {
            return;   
        }

        let totalPurchaseAmount = 0;
        let totalProfit = 0;
        let totalProfitPercentage = 0;
        let dayTrades = 0;
        let losingTrades = 0;

        newTrades.forEach(trade => {
            const profit = (trade.sellPrice - trade.purchasePrice) * trade.quantity;
            const profitPct = ((trade.sellPrice - trade.purchasePrice) / trade.purchasePrice) * 100;

            totalPurchaseAmount += trade.purchasePrice * trade.quantity;
            totalProfit += profit;
            totalProfitPercentage += profitPct;

            if (trade.purchaseDate === trade.sellDate) {
                dayTrades++;
            }

            if (profit < 0) {
                losingTrades++;
            }
        });
        tradeSummary.totalPurchaseAmount = totalPurchaseAmount.toLocaleString();
        tradeSummary.totalProfit = Number(totalProfit.toFixed(2)).toLocaleString();
        tradeSummary.averageTransactionSize = Number((totalPurchaseAmount / newTrades.length).toFixed(2)).toLocaleString();
        tradeSummary.averageProfit = Number((totalProfit / newTrades.length).toFixed(2));
        tradeSummary.averageProfitPercentage = Number((totalProfitPercentage / newTrades.length).toFixed(2));
        tradeSummary.numberOfDayTrades = dayTrades;
        tradeSummary.numberOfLosingTrades = losingTrades;
    }, [tradeSummary]);

    const retrieveTrades = useCallback(() => {
        const currentUserId = userId ? userId : (user ? user.id : 0);
        fetchWithAuth('/api/trade/retrieve', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId : currentUserId})
        })
        .then(response => {
                if (!response.ok) {
                    displayActionResponse("Data could not be retrieved!", ResponseTypes.ERROR_UNKNOWN);
                    return [];
                }
                return response.json();
        })
        .then(data => {
            setTrades(data);
            processTrades(data);
        })
        .catch(error => {
            logError(error);
            setTrades([]);
        });
    }, [user, userId, ResponseTypes.ERROR_UNKNOWN, processTrades]);
    
    useEffect(() => {
        if (!user) {
            return;
        }
        
        if (user.anonymous && !userId) {
            navigate('/login');
            return;
        }
        retrieveTrades();
    }, [user, userId, retrieveTrades, navigate]);
    

    const selectItemForInteraction = (item) => {
        if (item.id !== interactionMarker) {
            setInteractionMarker(item.id);
        }
    };

    const createProfitString = (item) => {
        const profit = ((item.sellPrice - item.purchasePrice) * item.quantity).toFixed(2);
        const positiveOrNegative = profit >= 0 ? '+' : '-';
        const formattedProfit = Math.abs(profit).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        return `${positiveOrNegative}$${formattedProfit}`;
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const month = date.getMonth() + 1; // Months are zero-indexed
        const day = date.getDate();
        const year = date.getFullYear().toString().slice(-2); // Get the last two digits of the year
        return `${month}/${day}/${year}`;
    };
    
    const displayActionResponse = (message, responseType) => {
        setResponseType(responseType);
        setActionResponse(message);

        if (message.length > 0) {
            setShowNotification(true);
            setTimeout(() => {
             setShowNotification(false);
            }, 3000); 
        }
    };
    
    const isLoss = (item) => {
        return item.purchasePrice > item.sellPrice;
    };
    
    const isDayTrade = (item) => {
        return item.purchaseDate === item.sellDate;
    };
    
    const getFilteredTrades = () => {
        return trades.filter(item => (
              new Date(item.sellDate).getFullYear() === Number(yearFilter) && 
             (tradeFilter === 'all' || 
             (tradeFilter === 'loss' && isLoss(item)) ||
             (tradeFilter === 'day' && isDayTrade(item)))));
    };
    
    const deleteTrade = (item) => {
        fetchWithAuth('/api/trade/delete', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
        })
        .then(response => {
            if (response.ok) {
                retrieveTrades();
                return response.json();
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })    
        .catch(error => {
            logError(error);
        });
    };
    
        const submitTrade = () => {
        
        if (!addTradeSymbol || addTradeSymbol.trim().length === 0) {
            displayActionResponse("Please enter a valid symbol name!", ResponseTypes.ERROR_UNKNOWN);
            return;
        }
        
        fetchWithAuth('/api/trade/submit', {
            method: 'POST',
            credentials: 'include',
            headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            symbol: addTradeSymbol, quantity : addTradeQuantity, 
            purchasePrice: addTradePurchasePrice, purchaseDate: addTradePurchaseDate,
            sellPrice: addTradeSalePrice, sellDate: addTradeSaleDate})
        })
        .then(response => {
            if (response.ok) {
                retrieveTrades();
                setAddTradeSymbol('');
                return response.json();
            } else {
                displayActionResponse("An issue occured!", ResponseTypes.ERROR_UNKNOWN);
                return [];
            }
        })     
        .catch(error => {
            logError(error);
        });
    };
    
    const handleAddTradeSymbolChange = (event) => {
        setAddTradeSymbol(event.target.value);
    };
    
    const handleAddTradeQuantityChange = (event) => {
        setAddTradeQuantity(event.target.value);
    };
    
    const handleAddTradePurchasePriceChange = (event) => {
        setAddTradePurchasePrice(event.target.value);
    };
    
    const handleAddTradePurchaseDateChange = (event) => {
        setAddTradePurchaseDate(event.target.value);
    };
    
    const handleAddTradeSalePriceChange = (event) => {
        setAddTradeSalePrice(event.target.value);
    };
    
    const handleAddTradeSaleDateChange = (event) => {
        setAddTradeSaleDate(event.target.value);
    };
    
    const handleTradeFilterChange = (event) => {
        setTradeFilter(event.target.value);
    };
    
    const handleYearFilterChange = (event) => {
        setYearFilter(event.target.value);
    };
    
    const createYearFilterValues = () => {
        const currentYear = new Date().getFullYear();

        const years = Array.from(
          new Set(trades.map(item => new Date(item.sellDate).getFullYear()))
        );

        if (!years.includes(currentYear)) {
          years.push(currentYear);
        }

        years.sort((a, b) => b - a);  
        return years;
    };
    
    const createLink = (symbol) => {
        return "https://finance.yahoo.com/quote/" + symbol; 
    };
    
    return (
        <div className="content"> 
            <UserInfo />
            {!user ? (
                <p>Loading...</p>
            ) : (!user.emailVerified && !userId) ? (
                <p>Your email address is not verified! Please check your inbox or request a new verification email <a href="/verifyEmail">here</a>!</p>
            ) : (trades.length === 0) ? (
                <p>The user does not have any trades.</p>
            ) :
            (
            <>
                <div className="trade-summary">
                    <h3 className="title">Trade Summary</h3>
                    <p>Total Amount Traded: ${tradeSummary.totalPurchaseAmount}</p>
                    <p>Total Profit: <span className="profit">${tradeSummary.totalProfit}</span> across <span className="bold">{trades.length}</span> trades </p>
                    <p>Average Trade Size: ${tradeSummary.averageTransactionSize}</p>
                    <p>Average Trade Profit: ${tradeSummary.averageProfit} / {tradeSummary.averageProfitPercentage}%</p>     
                </div>
                
                {user && !user.anonymous && (userId === user.id || !userId) &&
                <div className="activity-new">
                    <div className="trade-add-column">
                        <input type="text" placeholder="Symbol" id="trade-symbol" 
                           value={addTradeSymbol} onChange={handleAddTradeSymbolChange}
                           className="trade-add-input"/>
                        <input type="number" placeholder="Quantity" id="trade-quantity" 
                           value={addTradeQuantity} onChange={handleAddTradeQuantityChange}
                           className="trade-add-input"/>
                    </div>
                    <div className="trade-add-column">
                        <input type="number" placeholder="Purchase Price" id="purchase-price" 
                           value={addTradePurchasePrice} onChange={handleAddTradePurchasePriceChange}
                           className="trade-add-input"/>
                        <input type="date" placeholder="Purchase Date" id="purchase-date" 
                           value={addTradePurchaseDate} onChange={handleAddTradePurchaseDateChange}
                           className="trade-add-input"/>
                    </div>
                    <div className="trade-add-column">
                        <input type="number" placeholder="Sale Price" id="sale-price" 
                           value={addTradeSalePrice} onChange={handleAddTradeSalePriceChange}
                           className="trade-add-input"/>
                        <input type="date" placeholder="Sale Date" id="sale-date" 
                           value={addTradeSaleDate} onChange={handleAddTradeSaleDateChange}
                           className="trade-add-input"/> 
                    </div>
                    <div className="trade-add-button"> 
                        <button className="trade-add-button" onClick={submitTrade}>Add</button>
                    </div>
                </div>
                }
                <div className="trade-filter-group">
                    <select className="activity-select" onChange={handleTradeFilterChange} id="activity-category" >
                        <option value="all">All ({trades.length})</option>
                        <option value="day">Intraday ({tradeSummary.numberOfDayTrades})</option>
                        <option value="loss">Losses ({tradeSummary.numberOfLosingTrades})</option>
                    </select>
                    <select className="activity-select" id="activity-filter" 
                            onChange={handleYearFilterChange}>   
                        {createYearFilterValues().map(year => (
                            <option key={year} value={year}>{year}</option>
                          ))}
                    </select>  
                </div>
                
                <div className="activities-header trade-header"> 
                    <span>Symbol</span>
                    <span>Purchase</span>
                    <span>Sale</span>
                    <span>Profit</span>
                </div>
                <ul className="simple-list trade-list">
                { getFilteredTrades().map(item => (
                    <li key={item.id} className="simple-item" onClick={() => selectItemForInteraction(item)}>
                        <div className="item-content">
                            <span className="activity-name">
                            {item.symbol}<br/>x{item.quantity}
                            </span>
                            <span className="activity-name">
                            ${item.purchasePrice.toFixed(2)}<br/>{formatDate(new Date(item.purchaseDate))}&nbsp;<FaRegCalendarAlt className="activity-meta-icon"/>
                            </span>
                            <span className="activity-name">
                            ${item.sellPrice.toFixed(2)}<br/>{formatDate(new Date(item.sellDate))}&nbsp;<FaRegCalendarAlt className="activity-meta-icon"/>
                            </span>
                            <span className={`activity-name ${createProfitString(item).startsWith('+') ? 'profit' : 'loss'}`}>
                            {createProfitString(item)}
                            </span>
                        </div>
                        <div className={`list-item-actions ${!userId && interactionMarker === item.id ? 'visible' : 'hidden'}`}>
                            <a href={createLink(item.symbol)} target="_blank" rel="noreferrer">
                                <button>Quote</button>
                            </a>
                            <a>
                                <button className="" onClick={() => deleteTrade(item)}>Delete</button>
                            </a>
                        </div>
                    </li>
                ))}
                </ul>
            </>
            )}

            {showNotification && (
            <div className={responseType === ResponseTypes.SUCCESS ? "notification-success" : "notification-error"}>
                {actionResponse} 
            </div>
            )}
                                
            <div className="footer">
                <Footer/>
            </div>
        </div>);
};

export default Trading;
