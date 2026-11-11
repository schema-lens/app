export interface AdminUser {
  id: number;
  username: string;
  email: string;
  active: boolean;
  admin: boolean;
}

export interface AdminCreateUser {
  username: string;
  email: string;
  password: string;
  active: boolean;
  admin: boolean;
}

export interface AdminUpdateUser {
  email: string;
  active: boolean;
  admin: boolean;
  password?: string;
}
