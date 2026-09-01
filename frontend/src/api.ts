const API = "/api/v1";

/* =========================================================
   TYPES
   ========================================================= */

export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
};

export type TokenPayload = {
  sub?: string;
  userId?: string;
  roles?: string[];
  exp?: number;
};

export type Channel = {
  id: string;
  ownerId: string;
  name: string;
  description: string;
  createdAt: string;
};

export type PostStatus =
  | "DRAFT"
  | "PENDING_MODERATION"
  | "PUBLISHED"
  | "HIDDEN"
  | "REJECTED"
  | "DELETED";

export type Post = {
  id: string;
  channelId: string;
  authorId: string;
  title: string;
  content: string;
  status: PostStatus;
  createdAt: string;
  updatedAt: string;
  publishedAt?: string | null;
};

export type Media = {
  id: string;
  postId: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  url: string;
  urlExpiresAt: string;
};

export type Comment = {
  id: string;
  userId: string;
  postId: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export type Subscription = {
  id: string;
  userId: string;
  channelId: string;
  createdAt: string;
};

export type Notification = {
  id: string;
  type: string;
  postId: string;
  postTitle: string;
  recipientId: string;
  createdAt: string;
  isRead: boolean;
};

export type FeedItem = {
  postId: string;
  channelId: string;
  authorId: string;
  title: string;
  content: string;
  publishedAt: string;
};

export type FeedResponse = {
  content: FeedItem[];
  page: number;
  size: number;
};

export type UserResponse = {
  email: string;
  username: string;
  bio: string;
  createdAt: string;
};


/* =========================================================
   JWT
   ========================================================= */

export const decodeToken = (): TokenPayload | null => {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    return null;
  }

  try {
    const parts = token.split(".");

    if (parts.length !== 3) {
      return null;
    }

    const payload = parts[1];

    const normalized = payload
      .replace(/-/g, "+")
      .replace(/_/g, "/");

    const decoded = atob(
      normalized +
        "=".repeat(
          (4 - (normalized.length % 4)) % 4
        )
    );

    return JSON.parse(decoded);
  } catch {
    return null;
  }
};


/* =========================================================
   SESSION
   ========================================================= */

export const session = {
  getAccessToken: (): string | null => {
    return localStorage.getItem("accessToken");
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem("refreshToken");
  },

  getUser: (): TokenPayload | null => {
    return decodeToken();
  },

  save: (tokens: TokenResponse): void => {
    localStorage.setItem(
      "accessToken",
      tokens.accessToken
    );

    localStorage.setItem(
      "refreshToken",
      tokens.refreshToken
    );
  },

  clear: (): void => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
  },

  isLoggedIn: (): boolean => {
    const payload = decodeToken();

    if (!payload) {
      return false;
    }

    if (
      payload.exp &&
      payload.exp * 1000 <= Date.now() + 30_000
    ) {
      return false;
    }

    return true;
  },

  isAccessTokenExpired: (): boolean => {
    const payload = decodeToken();

    if (!payload?.exp) {
      return false;
    }

    return payload.exp * 1000 <= Date.now();
  },
};


/* =========================================================
   REFRESH TOKEN
   ========================================================= */

/*
 * Не допускаем несколько refresh-запросов одновременно.
 *
 * Например:
 *
 * GET /feed          -> 401
 * GET /notifications -> 401
 * GET /subscriptions -> 401
 *
 * Все три запроса будут ждать один refresh().
 */

let refreshPromise: Promise<boolean> | null = null;

async function refresh(): Promise<boolean> {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    const refreshToken =
      session.getRefreshToken();

    if (!refreshToken) {
      return false;
    }

    try {
      const response = await fetch(
        `${API}/auth/refresh`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            refreshToken,
          }),

          credentials: "omit",
        }
      );

      if (!response.ok) {
        session.clear();

        window.dispatchEvent(
          new Event("postly:session-expired")
        );

        return false;
      }

      const tokens: TokenResponse =
        await response.json();

      if (
        !tokens.accessToken ||
        !tokens.refreshToken
      ) {
        session.clear();

        window.dispatchEvent(
          new Event("postly:session-expired")
        );

        return false;
      }

      session.save(tokens);

      return true;
    } catch (error) {
      console.error(
        "Token refresh failed:",
        error
      );

      return false;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}


/* =========================================================
   RESTORE SESSION
   ========================================================= */

export async function restoreSession(): Promise<boolean> {
  const accessToken =
    session.getAccessToken();

  /*
   * Access token существует и валиден.
   */
  if (
    accessToken &&
    session.isLoggedIn()
  ) {
    return true;
  }

  /*
   * Access token отсутствует или истёк.
   *
   * Пробуем refresh token.
   */
  if (session.getRefreshToken()) {
    return refresh();
  }

  session.clear();

  return false;
}


/* =========================================================
   HTTP REQUEST
   ========================================================= */

async function request<T>(
  path: string,
  init: RequestInit = {},
  retry = true
): Promise<T> {

  /*
   * Если access token уже протух,
   * сначала пытаемся обновить его.
   */
  if (
    retry &&
    session.getRefreshToken() &&
    session.isAccessTokenExpired()
  ) {
    const refreshed =
      await refresh();

    if (!refreshed) {
      throw new Error(
        "Сессия истекла. Войдите в аккаунт снова."
      );
    }
  }

  const headers =
    new Headers(init.headers);

  const token =
    session.getAccessToken();

  /*
   * Автоматически добавляем JWT.
   */
  if (token) {
    headers.set(
      "Authorization",
      `Bearer ${token}`
    );
  }

  /*
   * Для JSON устанавливаем Content-Type.
   *
   * Для FormData НЕ устанавливаем!
   *
   * Browser сам добавит:
   *
   * multipart/form-data; boundary=...
   */
  if (
    init.body &&
    !(init.body instanceof FormData) &&
    !headers.has("Content-Type")
  ) {
    headers.set(
      "Content-Type",
      "application/json"
    );
  }

  const response = await fetch(
    `${API}${path}`,
    {
      ...init,
      headers,
      credentials: "omit",
    }
  );

  /*
   * Если получили 401,
   * пробуем обновить JWT.
   *
   * Только один повтор.
   */
  if (
    response.status === 401 &&
    retry &&
    session.getRefreshToken()
  ) {
    const refreshed =
      await refresh();

    if (refreshed) {
      return request<T>(
        path,
        init,
        false
      );
    }

    session.clear();

    window.dispatchEvent(
      new Event("postly:session-expired")
    );

    throw new Error(
      "Сессия истекла. Войдите в аккаунт снова."
    );
  }

  const text =
    await response.text();

  if (!response.ok) {
    let message =
      text ||
      `HTTP ${response.status}`;

    try {
      const json =
        JSON.parse(text);

      message =
        json?.error?.message ??
        json?.message ??
        message;
    } catch {
      // Ответ не JSON.
    }

    throw new Error(message);
  }

  /*
   * Например DELETE -> 204 No Content.
   */
  if (!text) {
    return undefined as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return text as T;
  }
}


/* =========================================================
   API
   ========================================================= */

export const api = {

  /* =======================================================
     AUTH
     ======================================================= */

  register: (
    body: {
      email: string;
      username: string;
      password: string;
    }
  ) =>
    request<TokenResponse>(
      "/auth/register",
      {
        method: "POST",
        body: JSON.stringify(body),
      }
    ),

  login: (
    body: {
      username: string;
      password: string;
    }
  ) =>
    request<TokenResponse>(
      "/auth/login",
      {
        method: "POST",
        body: JSON.stringify(body),
      }
    ),

  refresh,


  /* =======================================================
     USERS
     ======================================================= */

  user: (id: string) =>
    request<UserResponse>(
      `/users/${id}`
    ),

  updateBio: (
    id: string,
    bio: string
  ) =>
    request<UserResponse>(
      `/users/${id}`,
      {
        method: "PUT",
        body: JSON.stringify({
          bio,
        }),
      }
    ),


  /* =======================================================
     CHANNELS
     ======================================================= */

  createChannel: (
    body: {
      name: string;
      description: string;
    }
  ) =>
    request<Channel>(
      "/channels",
      {
        method: "POST",
        body: JSON.stringify(body),
      }
    ),

  getChannel: (id: string) =>
    request<Channel>(
      `/channels/${id}`
    ),

  updateChannel: (
    id: string,
    body: {
      name: string;
      description: string;
    }
  ) =>
    request<Channel>(
      `/channels/${id}`,
      {
        method: "PUT",
        body: JSON.stringify(body),
      }
    ),

  deleteChannel: (id: string) =>
    request<void>(
      `/channels/${id}`,
      {
        method: "DELETE",
      }
    ),


  /* =======================================================
     POSTS
     ======================================================= */

  /*
   * Создание поста.
   *
   * ВАЖНО:
   * Здесь пост НЕ публикуется.
   *
   * Backend должен создать его
   * со статусом DRAFT.
   */
  createPost: (
    body: {
      channelId: string;
      title: string;
      content: string;
    }
  ) =>
    request<Post>(
      "/posts",
      {
        method: "POST",
        body: JSON.stringify(body),
      }
    ),

  getPost: (id: string) =>
    request<Post>(
      `/posts/${id}`
    ),

  getChannelPosts: (
    channelId: string
  ) =>
    request<Post[]>(
      `/posts/channel/${channelId}`
    ),

  updatePost: (
    id: string,
    body: {
      title: string;
      content: string;
    }
  ) =>
    request<Post>(
      `/posts/${id}`,
      {
        method: "PUT",
        body: JSON.stringify(body),
      }
    ),

  deletePost: (id: string) =>
    request<void>(
      `/posts/${id}`,
      {
        method: "DELETE",
      }
    ),


  /*
   * DRAFT -> PENDING_MODERATION
   *
   * Пользователь отправляет свой пост
   * на модерацию.
   */
  moderatePost: (
    id: string
  ) =>
    request<Post>(
      `/posts/${id}/moderation`,
      {
        method: "POST",
      }
    ),


  /*
   * PENDING_MODERATION -> PUBLISHED
   *
   * Этот метод НЕ вызывается автоматически
   * после создания поста.
   *
   * Его должен вызвать администратор
   * после проверки поста.
   */
  publishPost: (
    id: string
  ) =>
    request<Post>(
      `/posts/${id}/publish`,
      {
        method: "POST",
      }
    ),


  /*
   * Скрыть опубликованный пост.
   */
  hidePost: (
    id: string
  ) =>
    request<Post>(
      `/posts/${id}/hide`,
      {
        method: "POST",
      }
    ),


  /* =======================================================
     MEDIA / MINIO
     ======================================================= */

  uploadImage: (
    postId: string,
    file: File
  ) => {

    const form =
      new FormData();

    form.append(
      "file",
      file
    );

    /*
     * ВАЖНО:
     * не указываем Content-Type вручную.
     *
     * Browser сам создаст:
     *
     * multipart/form-data;
     * boundary=...
     */

    return request<Media>(
      `/media/${postId}/images`,
      {
        method: "POST",
        body: form,
      }
    );
  },

  getImages: (
    postId: string
  ) =>
    request<Media[]>(
      `/media/${postId}/images`
    ),


  /* =======================================================
     LIKES
     ======================================================= */

  like: (
    postId: string
  ) =>
    request<void>(
      `/likes/${postId}/like`,
      {
        method: "POST",
      }
    ),

  unlike: (
    postId: string
  ) =>
    request<void>(
      `/likes/${postId}/unlike`,
      {
        method: "POST",
      }
    ),

  likeCount: (
    postId: string
  ) =>
    request<number>(
      `/likes/${postId}/count`
    ),


  /* =======================================================
     SUBSCRIPTIONS
     ======================================================= */

  subscribe: (
    channelId: string
  ) =>
    request<Subscription>(
      `/subscriptions/channels/${channelId}/subscribe`,
      {
        method: "POST",
      }
    ),

  unsubscribe: (
    channelId: string
  ) =>
    request<void>(
      `/subscriptions/channels/${channelId}/unsubscribe`,
      {
        method: "POST",
      }
    ),

  mySubscriptions: () =>
    request<Subscription[]>(
      "/subscriptions/me"
    ),


  /* =======================================================
     COMMENTS
     ======================================================= */

  comments: (
    postId: string
  ) =>
    request<Comment[]>(
      `/comments/posts/${postId}`
    ),

  createComment: (
    postId: string,
    content: string
  ) =>
    request<Comment>(
      `/comments/posts/${postId}`,
      {
        method: "POST",
        body: JSON.stringify({
          content,
        }),
      }
    ),

  deleteComment: (
    commentId: string
  ) =>
    request<void>(
      `/comments/${commentId}`,
      {
        method: "DELETE",
      }
    ),


  /* =======================================================
     FEED
     ======================================================= */

  feed: (
    page = 0,
    size = 20
  ) =>
    request<FeedResponse>(
      `/feed?page=${page}&size=${size}`
    ),


  /* =======================================================
     NOTIFICATIONS
     ======================================================= */

  notifications: (
    recipientId: string,
    unreadOnly = false
  ) =>
    request<Notification[]>(
      `/notifications?recipientId=${encodeURIComponent(
        recipientId
      )}&unreadOnly=${unreadOnly}`
    ),

  unreadCount: (
    recipientId: string
  ) =>
    request<number>(
      `/notifications/unread-count?recipientId=${encodeURIComponent(
        recipientId
      )}`
    ),

  markNotificationRead: (
    id: string
  ) =>
    request<Notification>(
      `/notifications/${id}/read`,
      {
        method: "PATCH",
      }
    ),
};


/* =========================================================
   KNOWN CHANNELS
   ========================================================= */

const KEY =
  "postly.knownChannels";

export function getKnownChannelIds(): string[] {
  try {
    const value =
      localStorage.getItem(KEY);

    if (!value) {
      return [];
    }

    const parsed =
      JSON.parse(value);

    return Array.isArray(parsed)
      ? parsed
      : [];
  } catch {
    return [];
  }
}

export function rememberChannel(
  id: string
): void {

  const ids =
    new Set(
      getKnownChannelIds()
    );

  ids.add(id);

  localStorage.setItem(
    KEY,
    JSON.stringify(
      [...ids]
    )
  );
}

export function forgetChannel(
  id: string
): void {

  const ids =
    getKnownChannelIds()
      .filter(
        (x) => x !== id
      );

  localStorage.setItem(
    KEY,
    JSON.stringify(
      ids
    )
  );
}