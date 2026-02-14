export const API_URL = import.meta.env.VITE_API_URL;

function getCookie(name) {
  const cookies = document.cookie.split("; ");
  for (const cookie of cookies) {
    const [key, value] = cookie.split("=");
    if (key === name) {
      return decodeURIComponent(value);
    }
  }
  return null;
}

// case 1: access token is valid -> request is processed
// case 2: access token is not valid, refresh token is valid -> refresh + request
// case 3: access token is not valid, refresh token is not valid, endpoint requires login -> failed request + login
// case 4: access tokem is not valid, refresh token is not valid, endpoint does not require login -> request
export async function fetchWithAuth(url, options = {}) {
    const accessTokenExpiresAt = getCookie('access_token_expires_at'); 
    
    if (accessTokenExpiresAt) {
        const now = new Date();
        const accessTokenExpiresAtDate = new Date(accessTokenExpiresAt);
        const hasAccessTokenExpired = now > accessTokenExpiresAtDate;   
        if (hasAccessTokenExpired) {
            await refreshAccessToken();
        }
    }
    
    options.credentials = 'include';

    try {
        let response = await fetch(API_URL + url, options);
        
        // login required
        if (response.status === 403) {
            window.location.href = '/login';
        }

        return response;
    } catch (err) {
        console.error('Fetch failed:', err);
        throw err;
    }
}

async function refreshAccessToken() {
    await fetch(`${API_URL}/api/user/refreshTokens`, {
        method: 'POST',
        credentials: 'include'
    });
    return true;
}
