BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "usuarios" (
	"id_usuario"	INTEGER,
	"nombre"	TEXT NOT NULL UNIQUE,
	"dni"	TEXT NOT NULL,
	"telefono"	TEXT,
	"genero"	TEXT NOT NULL CHECK("genero" IN ('Masculino', 'Femenino', 'Otro')),
	"email"	TEXT NOT NULL UNIQUE,
	"contrasena"	TEXT NOT NULL,
	"fecha_registro"	TEXT DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("id_usuario" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "clientes" (
	"id_cliente"	INTEGER,
	"nombre"	TEXT NOT NULL UNIQUE,
	"rtn"	TEXT NOT NULL UNIQUE,
	"telefono"	TEXT,
	"email"	TEXT,
	"fecha_cumpleanios"	TEXT,
	"fecha_registro"	TEXT DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY("id_cliente" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "categorias" (
	"id_categoria"	INTEGER,
	"nombre"	TEXT NOT NULL UNIQUE,
	"descripcion"	TEXT,
	PRIMARY KEY("id_categoria" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "productos" (
	"id_producto"	INTEGER,
	"codigo"	TEXT NOT NULL UNIQUE,
	"nombre"	TEXT NOT NULL UNIQUE,
	"descripcion"	TEXT,
	"precio_unitario"	REAL NOT NULL,
	"id_categoria"	INTEGER,
	"fecha_registro"	TEXT DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY("id_categoria") REFERENCES "categorias"("id_categoria"),
	PRIMARY KEY("id_producto" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "inventario" (
	"id_inventario"	INTEGER,
	"id_producto"	INTEGER NOT NULL UNIQUE,
	"cantidad_disponible"	INTEGER DEFAULT 0,
	"cantidad_minima"	INTEGER DEFAULT 10,
	"ultima_actualizacion"	TEXT DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY("id_producto") REFERENCES "productos"("id_producto"),
	PRIMARY KEY("id_inventario" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "facturas" (
	"id_factura"	INTEGER,
	"numero_factura"	TEXT NOT NULL UNIQUE,
	"id_cliente"	INTEGER NOT NULL,
	"id_usuario"	INTEGER NOT NULL,
	"fecha_factura"	TEXT DEFAULT CURRENT_TIMESTAMP,
	"subtotal"	REAL NOT NULL,
	"impuesto"	REAL DEFAULT 0,
	"total"	REAL NOT NULL,
	FOREIGN KEY("id_cliente") REFERENCES "clientes"("id_cliente"),
	FOREIGN KEY("id_usuario") REFERENCES "usuarios"("id_usuario"),
	PRIMARY KEY("id_factura" AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS "detalle_factura" (
	"id_detalle"	INTEGER,
	"id_factura"	INTEGER NOT NULL,
	"id_producto"	INTEGER NOT NULL,
	"cantidad"	INTEGER NOT NULL,
	"precio_unitario"	REAL NOT NULL,
	"subtotal"	REAL NOT NULL,
	FOREIGN KEY("id_producto") REFERENCES "productos"("id_producto"),
	PRIMARY KEY("id_detalle" AUTOINCREMENT),
	UNIQUE("id_factura","id_producto"),
	FOREIGN KEY("id_factura") REFERENCES "facturas"("id_factura") ON DELETE CASCADE
);
INSERT INTO "categorias" ("id_categoria","nombre","descripcion") VALUES (1,'Electrónica','Productos electrónicos y tecnología'),
 (2,'Accesorios','Productos electrónicos complementarios');
INSERT INTO "productos" ("id_producto","codigo","nombre","descripcion","precio_unitario","id_categoria","fecha_registro") VALUES (1,'PROD001','Laptop HP','Laptop HP Core i5 8GB RAM',15000.0,1,'2025-11-16 02:12:45'),
 (2,'PROD002','Mouse Logitech','Mouse inalámbrico',350.0,1,'2025-11-16 02:12:45');
INSERT INTO "inventario" ("id_inventario","id_producto","cantidad_disponible","cantidad_minima","ultima_actualizacion") VALUES (1,1,25,5,'2025-11-16 02:12:45'),
 (2,2,100,20,'2025-11-16 02:12:45');
COMMIT;
