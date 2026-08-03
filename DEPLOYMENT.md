# Cloud Deployment Guide: Vercel & Render

This guide explains how to deploy the Transaction Monitoring System (TMS) to the cloud using **Vercel** (for the React frontend) and **Render** (for the Spring Boot backend via Docker).

## 1. Database (PlanetScale / Aiven / Render PostgreSQL)
Render natively supports PostgreSQL, but since this project is built for **MySQL**, you'll need a managed MySQL database.
* Options: [Aiven](https://aiven.io/mysql), [PlanetScale](https://planetscale.com/), or AWS RDS.
* Create a database and get your credentials: Host, Port, Database Name, User, and Password.

## 2. Deploy Backend on Render

The backend is deployed as a Docker Web Service on Render. We have provided a `render.yaml` Blueprint to make this easy.

### Option A: Using Blueprint (Recommended)
1. Push this code to a GitHub repository.
2. Go to your Render Dashboard and click **New+** -> **Blueprint**.
3. Connect your repository. Render will automatically read the `render.yaml` file.
4. Fill in the missing environment variables for your MySQL database (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).
5. Set `FRONTEND_URL` to your future Vercel URL (e.g., `https://tms-frontend.vercel.app`) so CORS works correctly.
6. Click **Apply**.

### Option B: Manual Setup
1. Go to Render Dashboard -> **New Web Service**.
2. Select **Build and deploy from a Git repository**.
3. Set Environment to **Docker**.
4. Set the Docker Build Context to `./backend` and Dockerfile Path to `./backend/Dockerfile`.
5. Add the necessary Environment Variables (Database vars + `JWT_SECRET` + `FRONTEND_URL`).
6. Deploy.

Once deployed, copy the Render URL (e.g., `https://tms-backend.onrender.com`).

## 3. Deploy Frontend on Vercel

The frontend is a static React application (built with Vite) that connects to the backend API.

1. Push your code to GitHub.
2. Log into [Vercel](https://vercel.com/) and click **Add New** -> **Project**.
3. Import your GitHub repository.
4. In the configuration:
   * **Framework Preset**: Vite
   * **Root Directory**: `frontend`
   * **Build Command**: `npm run build`
   * **Output Directory**: `dist`
5. **Environment Variables**:
   * Add a variable named `VITE_API_URL`.
   * Set the value to your Render backend URL appended with `/api` (e.g., `https://tms-backend.onrender.com/api`).
6. Click **Deploy**.

*Note: We included a `vercel.json` file in the frontend folder to ensure React Router works correctly on page reloads.*

## 4. Final Verification
* Open your Vercel frontend URL.
* You should see the login screen.
* Log in with the default credentials (`admin` / `admin123`).
* Verify that you can view the dashboard and the transactions load successfully.
